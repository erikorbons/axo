package axo.core;

import org.reactivestreams.Subscriber;

public class Promise<T> extends Producer<T> {

	public Promise (final StreamContext context) {
		super(context);
	}

	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
	}

}
