package axo.core.producers;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Function2;
import axo.core.Producer;

public class ZippedProducer<A, B, R> extends Producer<R> {
	private final int bufferSize;
	private final Producer<A> sourceA;
	private final Producer<B> sourceB;
	private final Function2<? super A, ? super B, ? extends R> zipper;
	
	public ZippedProducer (final Producer<A> sourceA, final Producer<B> sourceB, final Function2<? super A, ? super B, ? extends R> zipper, final int bufferSize) {
		super (sourceA);
		
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
}
