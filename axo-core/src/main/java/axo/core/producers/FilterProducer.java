package axo.core.producers;

import java.util.Objects;
import java.util.Queue;

import org.reactivestreams.Subscriber;

import axo.core.AbstractProcessor;
import axo.core.Function;
import axo.core.Producer;
import axo.core.StreamExecutor;

public class FilterProducer<T> extends Producer<T> {
	private final Producer<T> source;
	private final Function<? super T, Boolean> fn;
	private final long batchCount;
	
	public FilterProducer (final Producer<T> source, final Function<? super T, Boolean> fn, final long batchCount) {
		super (Objects.requireNonNull (source, "source cannot be null").getContext ());
		
		if (fn == null) {
			throw new NullPointerException ("fn cannot be null");
		}
		if (batchCount <= 0) {
			throw new IllegalArgumentException ("batchCount should be >= 1");
		}
		
		this.source = source;
		this.fn = fn;
		this.batchCount = batchCount;
	}

	@Override
	public void subscribe (final Subscriber<? super T> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}

		final FilterProcessor processor = new FilterProcessor (getContext ().getSubscriptionFactory ().createStreamExecutor ());
		
		source.subscribe (processor);
		processor.subscribe (subscriber);
	}
	
	private class FilterProcessor extends AbstractProcessor<T, T> {
		public FilterProcessor (final StreamExecutor executor) {
			super(executor);
		}

		@Override
		public ProcessResult process (final Queue<T> input, long requestCount, final boolean sourceExhausted) {
			while (!input.isEmpty ()) {
				final T t = input.poll ();
				if (fn.apply (t)) {
					produce (t);
					-- requestCount;
				}
			}
			
			if (sourceExhausted) {
				return new ProcessResult (ProcessorState.FINISHED);
			}

			if (requestCount > 0) {
				return new ProcessResult (batchCount);
			}
			
			return new ProcessResult (ProcessorState.IDLE);
		}

		@Override
		public void terminate () {
		}
	}
}
