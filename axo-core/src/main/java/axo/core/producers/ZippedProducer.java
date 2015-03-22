package axo.core.producers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.StampedLock;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Action0;
import axo.core.Function2;
import axo.core.Producer;
import axo.core.StreamContext;

public class ZippedProducer<A, B, R> extends Producer<R> {
	private final int bufferSize;
	private final Producer<A> sourceA;
	private final Producer<B> sourceB;
	private final Function2<? super A, ? super B, ? extends R> zipper;
	
	public ZippedProducer (final Producer<A> sourceA, final Producer<B> sourceB, final Function2<? super A, ? super B, ? extends R> zipper, final int bufferSize) {
		if (bufferSize <= 0) {
			throw new NullPointerException ("bufferSize should be > 0");
		}
		if (sourceA == null) {
			throw new NullPointerException ("sourceA cannot be null");
		}
		if (sourceB == null) {
			throw new NullPointerException ("sourceB cannot be null");
		}
		if (zipper == null) {
			throw new NullPointerException ("zipper cannot be null");
		}

		this.bufferSize = bufferSize;
		this.sourceA = sourceA;
		this.sourceB = sourceB;
		this.zipper = zipper;
	}

	@Override
	public void subscribe (final Subscriber<? super R> subscriber) {

		subscriber.onSubscribe (new Subscription () {
			@Override
			public void request (final long count) {
			}
			
			@Override
			public void cancel () {
			}
		});
	}

	@Override
	public StreamContext getContext () {
		return sourceA.getContext ();
	}
}
