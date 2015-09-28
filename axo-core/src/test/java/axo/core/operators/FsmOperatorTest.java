package axo.core.operators;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.testng.annotations.Test;

import axo.core.StreamContext;
import axo.core.producers.AbstractProducerTest;

import static org.testng.Assert.*;

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
			pushState (state (this::handleInput0, this::handleComplete), input);
		}

		@Override
		public void handleComplete() {
			complete ();
		}

		public void handleInput0 (final Long input) {
			assertTrue (input % 5 == 0);
			produce (input);
			gotoState (state (this::handleInput1, this::handleComplete));
		}
		
		public void handleInput1 (final Long input) {
			assertTrue (input % 5 == 1);
			produce (input);
			gotoState (state (this::handleInput2, this::handleComplete));
		}
		
		public void handleInput2 (final Long input) {
			assertTrue (input % 5 == 2);
			produce (input);
			gotoState (state (this::handleInput3, this::handleComplete));
		}
		
		public void handleInput3 (final Long input) {
			assertTrue (input % 5 == 3);
			produce (input);
			gotoState (state (this::handleInput4, this::handleComplete));
		}
		
		public void handleInput4 (final Long input) {
			assertTrue (input % 5 == 4);
			produce (input);
			popState ();
		}
	}
}
