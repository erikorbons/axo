package axo.core.producers;

import java.util.Arrays;

import axo.core.StreamContext;

public class ArrayProducer<T> extends IterableProducer<T> {
	
	public ArrayProducer (final StreamContext context, final T[] items) {
		super (context, Arrays.asList (items));
	}
}
