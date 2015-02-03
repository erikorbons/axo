package axo.core.producers;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import axo.core.StreamContext;

public class PublisherWrapper<T> extends ContextProducer<T> {

	private final Publisher<T> publisher;
	
	public PublisherWrapper (final StreamContext context, final Publisher<T> publisher) {
		super (context);
		
		if (publisher == null) {
			throw new NullPointerException ("publisher cannot be null");
		}
		
		this.publisher = publisher;
	}

	@Override
	public void subscribe (final Subscriber<? super T> s) {
		publisher.subscribe (s);
	}
}
