package axo.core.producers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Function;
import axo.core.Producer;
import axo.core.StreamContext;

public class MappedProducer<T, R> extends Producer<R> {

	private final Producer<T> source;
	private final Function<? super T, ? extends R> mapper;
	
	public MappedProducer (final Producer<T> source, final Function<? super T, ? extends R> mapper) {
		if (source == null) {
			throw new NullPointerException ("source cannot be null");
		}
		if (mapper == null) {
			throw new NullPointerException ("mapper cannot be null");
		}
		
		this.source = source;
		this.mapper = mapper;
	}
	
	@Override
	public StreamContext getContext () {
		return source.getContext ();
	}
	
	@Override
	public void subscribe (final Subscriber<? super R> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}

		source.subscribe (new Subscriber<T> () {
			private Subscription s = null;
			private boolean terminated = false;
			
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
					subscriber.onNext (mapper.apply (t));
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
