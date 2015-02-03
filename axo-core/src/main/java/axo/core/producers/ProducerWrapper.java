package axo.core.producers;

import org.reactivestreams.Subscriber;

import axo.core.Producer;
import axo.core.StreamContext;

public class ProducerWrapper<T> extends Producer<T> {

	private final Producer<T> wrappedProducer;
	
	public ProducerWrapper (final Producer<T> wrappedProducer) {
		if (wrappedProducer == null) {
			throw new NullPointerException ("wrappedProducer cannot be null");
		}
		
		this.wrappedProducer = wrappedProducer;
	}

	@Override
	public void subscribe (final Subscriber<? super T> s) {
		wrappedProducer.subscribe (s);
	}

	@Override
	public StreamContext getContext () {
		return wrappedProducer.getContext ();
	}
}
