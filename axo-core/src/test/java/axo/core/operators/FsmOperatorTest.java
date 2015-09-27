package axo.core.operators;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.testng.annotations.Test;

import axo.core.StreamContext;
import axo.core.producers.AbstractProducerTest;

public class FsmOperatorTest extends AbstractProducerTest<Long> {
	@Override
	public Publisher<Long> createFailedPublisher () {
		return null;
	}

	@Override
	public Publisher<Long> createPublisher (final long numElements) {
		return streamContext
			.range (0, numElements)
			.lift ((context, subscriber) -> new PassThroughFsm (context, subscriber));
	}
	
	@Override
	public long maxElementsFromPublisher() {
		return Long.MAX_VALUE;
	}

	@Test
	public void test () {
		
	}
	
	public final static class PassThroughFsm extends FsmOperator<Long, Long> {

		public PassThroughFsm (StreamContext context, Subscriber<? super Long> subscriber) {
			super(context, subscriber);
		}

		@Override
		public void handleInput (final Long input) {
			produce (input);
		}

		@Override
		public void handleComplete() {
			complete ();
		}
	}
}
