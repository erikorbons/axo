package axo.core;

import org.reactivestreams.Publisher;

public interface ProducerFactory {
	
	<E> Producer<E> from (@SuppressWarnings("unchecked") final E ... ts);
	<E> Producer<E> from (final Iterable<E> ts);
	<E> Producer<E> from (final Publisher<E> ts);
	<E> Producer<E> from (final Producer<E> ts);
	Producer<Integer> range (final int min, final int max);
	Producer<Long> range (final long min, final long max);
	<E> Producer<E> empty ();
}
