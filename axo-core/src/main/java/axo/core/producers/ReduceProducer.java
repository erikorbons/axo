package axo.core.producers;

import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Function2;
import axo.core.Producer;
import axo.core.StreamContext;

public class ReduceProducer<T> extends Producer<T> {
	private final Producer<T> source;
	private final Function2<? super T, ? super T, ? extends T> reducer;
	
	public ReduceProducer (final Producer<T> source, final Function2<? super T, ? super T, ? extends T> reducer) {
		if (source == null) {
			throw new NullPointerException ("source cannot be null");
		}
		if (reducer == null) {
			throw new NullPointerException ("reducer cannot be null");
		}
		
		this.source = source;
		this.reducer = reducer;
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
		
		source.subscribe (new Subscriber<T> () {
			private AtomicReference<Subscription> currentSubscription = new AtomicReference<> (null);
			private T value = null;
			private long processedCount = 0;
			
			@Override
			public void onComplete () {
				if (processedCount == 0) {
					subscriber.onComplete ();
				} else {
					subscriber.onNext (value);
					subscriber.onComplete ();
				}
			}

			@Override
			public void onError (final Throwable error) {
				subscriber.onError (error);
			}

			@Override
			public void onNext (final T t) {
				if (processedCount == 0) {
					value = t;
				} else {
					try {
						value = reducer.apply (value, t);
					} catch (Throwable e) {
						subscriber.onError (e);
						currentSubscription.get ().cancel ();
						return;
					}
				}
				
				++ processedCount;
			}

			@Override
			public void onSubscribe (final Subscription subscription) {
				subscriber.onSubscribe (new Subscription () {
					@Override
					public void request (final long n) {
						if (n <= 0) {
							throw new IllegalArgumentException ("n should be > 0");
						}
						
						if (currentSubscription.compareAndSet (null, subscription)) {
							subscription.request (Long.MAX_VALUE);
						}
					}
					
					@Override
					public void cancel () {
						subscription.cancel ();
					}
				});
			}
		});
	}
}
