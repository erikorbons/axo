package axo.core.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscriber;

import axo.core.Action;
import axo.core.Function2;
import axo.core.StreamExecutor;
import axo.core.StreamExecutorFactory;

public class ExecutorServiceExecutor implements StreamExecutorFactory, StreamExecutor {
	private final ExecutorService executorService;
	
	private final long produceLimit = 1000;
	
	public ExecutorServiceExecutor (final ExecutorService executorService) {
		if (executorService == null) {
			throw new NullPointerException ("executorService cannot be null"); 
		}
		
		this.executorService = executorService;
	}
	
	@Override
	public <T> ImmediateExecutor createImmediateExecutor(
			final Subscriber<? super T> subscriber,
			final Function2<Subscriber<? super T>, Long, Boolean> producerFunction) {
		
		final AtomicBoolean terminated = new AtomicBoolean (false);
		final AtomicBoolean producing = new AtomicBoolean (false);
		final AtomicLong count = new AtomicLong ();
		
		final Action<StreamExecutor> produceItemsAction = new Action<StreamExecutor> () {
			@Override
			public void apply (final StreamExecutor executor) {
				long n = 0;		// Number of items produced by this action:
	
				// Produce items in a loop:
				while (true) {
					// Create a batch of items to fetch:
					long currentCount, newCount, batchCount;
					do {
						currentCount = count.get ();
						batchCount = Math.min (currentCount, produceLimit - n);
						newCount = currentCount - batchCount;
					} while (!count.compareAndSet (currentCount, newCount));
					
					// Terminate if there is nothing left to produce:
					if (batchCount == 0) {
						break;
					}
					
					// Produce items:
					try {
						if (!producerFunction.apply (subscriber, batchCount)) {
							terminated.set (true);
							subscriber.onComplete ();
							break;
						}
					} catch (Throwable e) {
						subscriber.onError (e);
						terminated.set (true);
						break;
					}
	
					// See if enough items have been produced:
					n += batchCount;
					if (n >= produceLimit) {
						break;
					}
				}
				
				// Indicate that we're not producing:
				producing.set (false);
				
				// Produce again if the count is > 0:
				if (!terminated.get () && count.get () > 0) {
					if (producing.compareAndSet (false, true)) {
						scheduleAction (this);
					}
				}
			}
		};
		
		return new ImmediateExecutor () {
			@Override
			public void request (final long n) {
				// Add to the total count of items that can be fetched from this
				// subscription:
				count.addAndGet (n);
				
				// Start producing if the produceItemsAction isn't scheduled or running:
				if (producing.compareAndSet (false, true)) {
					scheduleAction (produceItemsAction);
				}
			}
			
			@Override
			public void cancel () {
				terminated.set (true);
			}
		};
	}

	@Override
	public void start () {
	}
	
	@Override
	public void shutdown () throws InterruptedException {
		executorService.shutdown ();
		executorService.awaitTermination (10, TimeUnit.SECONDS);
	}

	@Override
	public void scheduleAction (final Action<StreamExecutor> action) {
		final StreamExecutor self = this;
		
		executorService.execute (() -> action.apply (self));
	}
}
