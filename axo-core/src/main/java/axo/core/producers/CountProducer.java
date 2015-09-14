package axo.core.producers;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Producer;

public class CountProducer extends Producer<Long> {
	private final Producer<?> source;
	
	public CountProducer (final Producer<?> source) {
		super (Objects.requireNonNull (source, "source cannot be null").getContext ());
		
		this.source = source;
	}

	@Override
	public void subscribe (final Subscriber<? super Long> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		source.subscribe (new Subscriber<Object> () {
			private AtomicReference<Subscription> currentSubscription = new AtomicReference<> (null);
			private long processedCount = 0;
			
			@Override
			public void onComplete () {
				subscriber.onNext (processedCount);
				subscriber.onComplete ();
			}

			@Override
			public void onError (final Throwable error) {
				subscriber.onError (error);
			}

			@Override
			public void onNext (final Object t) {
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
