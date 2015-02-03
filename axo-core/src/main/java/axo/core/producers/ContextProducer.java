package axo.core.producers;

import axo.core.Producer;
import axo.core.StreamContext;

public abstract class ContextProducer<T> extends Producer<T> {
	private final StreamContext context;
	
	public ContextProducer (final StreamContext context) {
		if (context == null) {
			throw new NullPointerException ("context cannot be null");
		}
		
		this.context = context;
	}
	
	@Override
	public StreamContext getContext() {
		return context;
	}
}
