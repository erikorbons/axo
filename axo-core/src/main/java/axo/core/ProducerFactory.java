package axo.core;

import java.io.InputStream;

import org.reactivestreams.Publisher;

import axo.core.data.ByteString;

public interface ProducerFactory {
	
	<E> Producer<E> from (@SuppressWarnings("unchecked") E ... ts);
	<E> Producer<E> from (Iterable<E> ts);
	<E> Producer<E> from (Publisher<E> ts);
	<E> Producer<E> from (Producer<E> ts);
	Producer<ByteString> from (InputStream inputStream);
	
	Producer<Integer> range (final int min, final int max);
	Producer<Long> range (final long min, final long max);
	<E> Producer<E> empty ();
}
