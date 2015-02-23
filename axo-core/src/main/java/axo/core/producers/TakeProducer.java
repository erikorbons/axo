package axo.core.producers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Producer;
import axo.core.StreamContext;

public class TakeProducer<T> extends Producer<T> {

	private final Producer<T> source;
	private final long count;
	
	public TakeProducer (final Producer<T> source, final long n) {
		if (source == null) {
			throw new NullPointerException ("source cannot be null");
		}
		if (n < 0) {
			throw new IllegalArgumentException ("n should be >= 0");
		}
		
		this.source = source;
		this.count = n;
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
			private Subscription s = null;
			private boolean terminated = false;
			private long processedCount = 0;
			
			@Override
			public void onSubscribe (final Subscription s) {
				this.s = s;
				
				subscriber.onSubscribe (new Subscription () {
					@Override
					public void request (final long n) {
						s.request (n);
					}
					
					@Override
					public void cancel() {
						s.cancel ();
					}
				});
			}

			@Override
			public void onNext (final T t) {
				if (terminated) {
					return;
				}
				
				try {
					if (processedCount >= count) {
						subscriber.onComplete ();
						terminated = true;
					} else {
						subscriber.onNext (t);
						++ processedCount;
					}
				} catch (Throwable e) {
					subscriber.onError (e);
					s.cancel ();
					terminated = true;
				}
			}

			@Override
			public void onError (final Throwable t) {
				subscriber.onError (t);
				terminated = true;
			}

			@Override
			public void onComplete() {
				subscriber.onComplete ();
				terminated = true;
			}
		});
	}
}
