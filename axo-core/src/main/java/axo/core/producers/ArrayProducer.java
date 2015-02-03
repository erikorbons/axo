package axo.core.producers;

import java.util.Arrays;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Producer;

public class ArrayProducer<T> extends Producer<T> {

	private final T[] items;
	
	public ArrayProducer (final T[] items) {
		if (items == null) {
			throw new NullPointerException ("items cannot be null");
		}
		
		this.items = Arrays.copyOf (items, items.length);
	}

	public void subscribe (final Subscriber<? super T> subscriber) {
		subscriber.onSubscribe (new ArraySubscription (subscriber));
	}
	
	private class ArraySubscription implements Subscription {
		private final Subscriber<? super T> subscriber;

		private int offset = 0;
		private int requested = 0;
		
		public ArraySubscription (final Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
		}
		
		public void request (final long n) {
		}

		public void cancel() {
		}
	}
}
