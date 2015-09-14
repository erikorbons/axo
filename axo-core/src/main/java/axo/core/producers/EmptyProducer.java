package axo.core.producers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Producer;
import axo.core.StreamContext;

public class EmptyProducer<T> extends Producer<T> {
	public EmptyProducer (final StreamContext context) {
		super (context);
	}
	
	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		subscriber.onSubscribe (new Subscription () {
			@Override
			public void request (final long n) {
				if (n <= 0) {
					throw new IllegalArgumentException ("n should be > 0");
				}
				
				subscriber.onComplete ();
			}
			
			@Override
			public void cancel () {
			}
		});
	}
}
