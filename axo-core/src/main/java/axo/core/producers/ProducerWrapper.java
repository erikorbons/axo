package axo.core.producers;

import java.util.Objects;

import org.reactivestreams.Subscriber;

import axo.core.Producer;

public class ProducerWrapper<T> extends Producer<T> {

	private final Producer<T> wrappedProducer;
	
	public ProducerWrapper (final Producer<T> wrappedProducer) {
		super (Objects.requireNonNull (wrappedProducer, "wrappedProducer cannot be null").getContext ());
		
		this.wrappedProducer = wrappedProducer;
	}

	@Override
	public void subscribe (final Subscriber<? super T> s) {
		wrappedProducer.subscribe (s);
	}
}
