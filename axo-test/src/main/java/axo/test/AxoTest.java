package axo.test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Action1;
import axo.core.Producer;
import axo.core.StreamContext;
import axo.core.executors.ExecutorServiceExecutor;

public final class AxoTest implements AutoCloseable {

	private static final AtomicReference<AxoTest> currentTest = new AtomicReference<> (null);
	private final ThreadPoolExecutor threadPoolExecutor; 
	private final ExecutorServiceExecutor streamExecutor;
	private final StreamContext streamContext;
	
	public AxoTest () {
		this.threadPoolExecutor = new ThreadPoolExecutor (1, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable> ());
		this.streamExecutor = new ExecutorServiceExecutor (threadPoolExecutor);
		this.streamContext = new StreamContext (streamExecutor);
	}
	
	public static void axoTest (final Action1<StreamContext> action) throws Throwable {
		if (action == null) {
			throw new NullPointerException ("action cannot be null");
		}

		try (final AxoTest test = new AxoTest ()) {
			if (!currentTest.compareAndSet (null, test)) {
				throw new IllegalStateException ("A test is currently running");
			}
			
			action.apply (test.getContext ());
			
			if (!currentTest.compareAndSet (test, null)) {
				throw new IllegalStateException ("Nested AXO tests detected");
			}
		}
	}
	
	private static AxoTest currentTest () {
		final AxoTest test = currentTest.get ();
		if (test == null) {
			throw new IllegalStateException ("No test is currently in progress");
		}
		return test;
	}
	
	public static <T> void consume (final Producer<T> producer, final Action1<SynchronousConsumer<T>> action) {
		final SynchronousConsumer<T> subscriber = new SynchronousConsumer<> ();
		
		producer.subscribe (subscriber);
		try {
			subscriber.waitForSubscription ();
			
			action.apply (subscriber);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException (e);
		}
	}
	
	
	public StreamContext getContext () {
		return streamContext;
	}

	@Override
	public void close () throws Exception {
		streamExecutor.shutdown ();
	}
	
	public static class SynchronousConsumer<T> implements Subscriber<T> {
		private CompletableFuture<Subscription> subscription = new CompletableFuture<> ();
		private BlockingQueue<Cmd<T>> queue = new LinkedBlockingQueue<> ();
		
		public void waitForSubscription () throws InterruptedException, ExecutionException {
			subscription.get ();
		}
		
		public T takeOne () {
			try {
				subscription.get ().request (1);
				
				final Cmd<T> cmd = queue.poll (10000, TimeUnit.MILLISECONDS);
				
				if (cmd == null) {
					throw new RuntimeException ("Timeout while waiting for element");
				}
				
				switch (cmd.type) {
				case COMPLETE:
					return null;
				case ERROR:
					throw new RuntimeException (cmd.exception);
				case ITEM:
					return cmd.value;
				default:
					throw new RuntimeException ("Unknown command");
				}
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException (e);
			}
		}
		
		@Override
		public void onSubscribe (final Subscription s) {
			subscription.complete (s);
		}

		@Override
		public void onNext (final T t) {
			try {
				queue.put (new Cmd<T> (CmdType.ITEM, t, null));
			} catch (InterruptedException e) {
				throw new RuntimeException (e);
			}
		}

		@Override
		public void onError (final Throwable t) {
			try {
				queue.put (new Cmd<T> (CmdType.ERROR, null, t));
			} catch (InterruptedException e) {
				throw new RuntimeException (e);
			}
		}

		@Override
		public void onComplete () {
			try {
				queue.put (new Cmd<T> (CmdType.COMPLETE, null, null));
			} catch (InterruptedException e) {
				throw new RuntimeException (e);
			}
		}

		public static enum CmdType {
			ITEM,
			ERROR,
			COMPLETE
		}
		
		public static class Cmd<T> {
			public final CmdType type;
			public final T value;
			public final Throwable exception;
			
			public Cmd (final CmdType type, final T value, final Throwable exception) {
				this.type = type;
				this.value = value;
				this.exception = exception;
			}
		}
	}
}
