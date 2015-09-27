package axo.core;

import java.util.Objects;

import org.reactivestreams.Subscriber;

public abstract class Operator<T, R> implements Subscriber<T> {

	private final StreamContext context;
	private final Subscriber<? super R> subscriber;
	
	public Operator (final StreamContext context, final Subscriber<? super R> subscriber) {
		this.context = Objects.requireNonNull(context, "context cannot be null");
		this.subscriber = subscriber;
	}
	
	public StreamContext getContext () {
		return context;
	}
	
	protected Subscriber<? super R> getSubscriber () {
		return subscriber;
	}
}
