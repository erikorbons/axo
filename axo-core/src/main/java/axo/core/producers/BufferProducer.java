package axo.core.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.reactivestreams.Subscriber;

import axo.core.AbstractProcessor;
import axo.core.Producer;
import axo.core.StreamContext;
import axo.core.StreamExecutor;

public class BufferProducer<T> extends Producer<List<T>> {
	
	private final int bufferSize;
	private final Producer<T> source;
	
	public BufferProducer (final Producer<T> source, final int bufferSize) {
		if (source == null) {
			throw new NullPointerException ("source cannot be null");
		}
		if (bufferSize < 1) {
			throw new IllegalArgumentException ("bufferSize should be > 1");
		}
		
		this.source = source;
		this.bufferSize = bufferSize;
	}
	
	@Override
	public void subscribe (final Subscriber<? super List<T>> subscriber) {
		if (subscriber == null) {
			throw new NullPointerException ("subscriber cannot be null");
		}
		
		final BufferProcessor processor = new BufferProcessor (getContext ().getSubscriptionFactory ().createStreamExecutor ());
		
		source.subscribe (processor);
		processor.subscribe (subscriber);
	}

	@Override
	public StreamContext getContext () {
		return source.getContext ();
	}
	
	private class BufferProcessor extends AbstractProcessor<T, List<T>> {
		public BufferProcessor (final StreamExecutor executor) {
			super(executor);
		}

		@Override
		public ProcessResult process (final Queue<T> input, final long requestCount, final boolean sourceExhausted) {
			while (input.size () >= bufferSize) {
				final List<T> result = new ArrayList<T> (bufferSize);
				
				for (int i = 0; i < bufferSize; ++ i) {
					result.add (input.poll ());
				}
				
				produce (result);
			}
			
			if (sourceExhausted && !input.isEmpty ()) {
				final List<T> result = new ArrayList<> (input.size ());
				result.addAll (input);
				input.clear ();
				produce (result);
				return new ProcessResult (ProcessorState.FINISHED);
			}
			
			return sourceExhausted 
					? new ProcessResult (ProcessorState.FINISHED) 
					: new ProcessResult (bufferSize - input.size ());
		}
		
		@Override
		public void terminate () {
		}
	}
}
