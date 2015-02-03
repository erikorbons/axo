package axo.core.producers;

import java.util.Iterator;

import org.reactivestreams.Subscriber;

import axo.core.SubscriptionFactory.ImmediateSubscription;
import axo.core.StreamContext;

public class IterableProducer<T> extends ContextProducer<T> {
	
	private final Iterable<T> iterable;
	
	public IterableProducer (final StreamContext context, final Iterable<T> iterable) {
		super (context);
		
		if (iterable == null) {
			throw new NullPointerException ("iterable cannot be null");
		}
		
		this.iterable = iterable;
	}

	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		// Create an iterator:
		final Iterator<T> iterator = iterable.iterator ();
		
		// Create an executor:
		final ImmediateSubscription executor = getContext ()
			.getSubscriptionFactory ()
			.createImmediateSubscription (subscriber, (innerSubscriber, count) -> {
				for (long l = 0; l < count; ++ l) {
					if (!iterator.hasNext ()) {
						return false;
					}
					subscriber.onNext (iterator.next ());
				}
				
				return true;
			});
		
		subscriber.onSubscribe (executor);
	}

}
