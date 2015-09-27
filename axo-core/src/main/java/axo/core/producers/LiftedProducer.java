package axo.core.producers;

import java.util.Objects;

import org.reactivestreams.Subscriber;

import axo.core.Operator;
import axo.core.OperatorSupplier;
import axo.core.Producer;

public class LiftedProducer<T, R> extends Producer<R> {
	
	private final Producer<T> source;
	private final OperatorSupplier<T, R> operatorSupplier;

	public LiftedProducer (final Producer<T> source, final OperatorSupplier<T, R> operatorSupplier) {
		super (source);
		
		this.source = Objects.requireNonNull (source, "source cannot be null"); 
		this.operatorSupplier = Objects.requireNonNull (operatorSupplier, "operatorSupplier cannot be null");
	}

	@Override
	public void subscribe (final Subscriber<? super R> subscriber) {
		final Operator<T, R> operator = operatorSupplier.apply (
				getContext (), 
				Objects.requireNonNull (subscriber, "subscriber cannot be null")
			);

		source.subscribe (operator);
	}
}
