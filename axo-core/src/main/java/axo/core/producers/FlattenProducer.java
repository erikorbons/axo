package axo.core.producers;

import java.util.Queue;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.AbstractProcessor;
import axo.core.Producer;
import axo.core.StreamContext;
import axo.core.StreamExecutor;

public class FlattenProducer<T> extends Producer<T> {
	
	private final Producer<Producer<? extends T>> source;
	private final long requestCount;
	
	public FlattenProducer (final Producer<Producer<? extends T>> source, final long requestCount) {
		if (source == null) {
			throw new NullPointerException ("source cannot be null");
		}
		if (requestCount <= 0) {
			throw new IllegalArgumentException ("requestCount must be >= 1");
		}
		
		this.source = source;
		this.requestCount = requestCount;
	}

	@Override
	public StreamContext getContext () {
		return source.getContext ();
	}
	
	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}

		final FlattenProcessor processor = new FlattenProcessor (getContext ().getSubscriptionFactory ().createStreamExecutor ());
		
		source.subscribe (processor);
		processor.subscribe (subscriber);
	}
	
	private class FlattenProcessor extends AbstractProcessor<Producer<? extends T>, T> {
		// TODO: Cancel the current subscription if the processor is cancelled.
		private Subscription subscription = null;
		private long requested = 0;
		
		public FlattenProcessor (final StreamExecutor executor) {
			super(executor);
		}

		@Override
		public long process (final Queue<Producer<? extends T>> input, final boolean sourceExhausted) {
			// Subscribe to a new producer:
			if (subscription == null && !input.isEmpty ()) {
				input
					.poll ()
					.subscribe (new Subscriber<T> () {
						@Override
						public void onComplete () {
							schedule (() -> {
								subscription = null;
								requested = 0;
							});
						}

						@Override
						public void onError(final Throwable t) {
							schedule (() -> { 
								throw new RuntimeException (t);
							});
						}

						@Override
						public void onNext (final T element) {
							schedule (() -> {
								produce (element);
								-- requested;
								if (requested <= 0) {
									requested = requestCount;
									subscription.request (requestCount);
								}
							});
						}

						@Override
						public void onSubscribe (final Subscription s) {
							subscription = s;
							subscription.request (requestCount);
							requested = requestCount;
						}
					});
				
				return 0;
			} 
			
			return subscription != null || sourceExhausted || !input.isEmpty () ? 0 : 1;
		}
	}
}
