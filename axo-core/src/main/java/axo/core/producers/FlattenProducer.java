package axo.core.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Function;
import axo.core.Producer;
import axo.core.StreamContext;

public class FlattenProducer<T> extends Producer<T> {
	
	private final Producer<Producer<? extends T>> source;
	
	private final int requestCount;
	
	public FlattenProducer (final Producer<Producer<? extends T>> source, final int requestCount) {
		if (source == null) {
			throw new NullPointerException ("source cannot be null");
		}
		if (requestCount < 0) {
			throw new IllegalArgumentException ("requestCount should be >= 1");
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

		source.subscribe (new Subscriber<Producer<? extends T>> () {
			private Subscription subscription = null;
			private final List<Producer<? extends T>> producers = new ArrayList<> (requestCount);
			private final List<T> items = new ArrayList<> (requestCount);
			
			private AtomicBoolean terminated = new AtomicBoolean (false);
			private AtomicLong requested = new AtomicLong (0);
			
			@Override
			public void onSubscribe (final Subscription s) {
				this.subscription = s;

				subscriber.onSubscribe (new Subscription () {
					@Override
					public void request (final long n) {
						while (true) {
							final long current = requested.get () + n;
							if (requested.compareAndSet (current, current + n)) {
								break;
							}
						} 
						
						produce ();
					}
					
					@Override
					public void cancel () {
						subscription.cancel ();
						terminated.set (true);
					}
				});
			}
			
			private void produce () {
				
			}

			@Override
			public void onNext (final Producer<? extends T> t) {
				try {
					producers.add (t);
				} catch (Throwable e) {
					subscriber.onError (e);
					subscription.cancel ();
					terminated.set (true);
				}
			}

			@Override
			public void onError (final Throwable t) {
				subscriber.onError (t);
				terminated.set (false);
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
