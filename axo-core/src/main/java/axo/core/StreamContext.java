package axo.core;

import java.util.Arrays;
import java.util.Iterator;

import org.reactivestreams.Publisher;

import axo.core.producers.IterableProducer;
import axo.core.producers.ProducerWrapper;
import axo.core.producers.PublisherWrapper;

public class StreamContext implements ProducerFactory {

	private final StreamExecutorFactory subscriptionFactory;
	
	public StreamContext (final StreamExecutorFactory subscriptionFactory) {
		if (subscriptionFactory == null) {
			throw new NullPointerException ("subscriptionFactory cannot be null");
		}
		
		this.subscriptionFactory = subscriptionFactory;
	}
	
	public StreamExecutorFactory getSubscriptionFactory () {
		return subscriptionFactory;
	}
	
	@Override
	@SafeVarargs
	public final <T> Producer<T> from (final T ... ts) {
		return new IterableProducer<> (this, Arrays.asList (ts));
	}
	
	@Override
	public final <T> Producer<T> from (final Iterable<T> ts) {
		return new IterableProducer<> (this, ts);
	}
	
	@Override
	public final <T> Producer<T> from (final Publisher<T> ts) {
		return new PublisherWrapper<> (this, ts);
	}
	
	@Override
	public final <T> Producer<T> from (final Producer<T> ts) {
		return new ProducerWrapper<> (ts);
	}
	
	@Override
	public final Producer<Integer> range (final int min, final int max) {
		if (min > max) {
			throw new IllegalArgumentException ("min should be <= max");
		}
		
		return new IterableProducer<> (this, new Iterable<Integer> () {
			@Override
			public Iterator<Integer> iterator () {
				return new Iterator<Integer> () {
					private int i = min;
					
					@Override
					public boolean hasNext () {
						return i <= max;
					}

					@Override
					public Integer next () {
						return i ++;
					}
				};
			}
		});
	}
	
	@Override
	public final Producer<Long> range (final long min, final long max) {
		if (min > max) {
			throw new IllegalArgumentException ("min should be <= max");
		}
		
		return new IterableProducer<> (this, new Iterable<Long> () {
			@Override
			public Iterator<Long> iterator () {
				return new Iterator<Long> () {
					private long i = min;
					
					@Override
					public boolean hasNext () {
						return i <= max;
					}

					@Override
					public Long next () {
						return i ++;
					}
				};
			}
		});
	}
}
