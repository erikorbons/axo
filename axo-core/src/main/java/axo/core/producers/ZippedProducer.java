package axo.core.producers;

import org.reactivestreams.Subscriber;

import axo.core.Function2;
import axo.core.Producer;
import axo.core.StreamContext;

public class ZippedProducer<A, B, R> extends Producer<R> {
	private final Producer<A> sourceA;
	private final Producer<B> sourceB;
	private final Function2<? super A, ? super B, ? extends R> zipper;
	
	public ZippedProducer (final Producer<A> sourceA, final Producer<B> sourceB, final Function2<? super A, ? super B, ? extends R> zipper) {
		if (sourceA == null) {
			throw new NullPointerException ("sourceA cannot be null");
		}
		if (sourceB == null) {
			throw new NullPointerException ("sourceB cannot be null");
		}
		if (zipper == null) {
			throw new NullPointerException ("zipper cannot be null");
		}
		
		this.sourceA = sourceA;
		this.sourceB = sourceB;
		this.zipper = zipper;
	}

	@Override
	public void subscribe (final Subscriber<? super R> subscriber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public StreamContext getContext () {
		return sourceA.getContext ();
	}
}
