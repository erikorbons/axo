package axo.core;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

import org.reactivestreams.Publisher;

import axo.core.concurrent.Scheduler;
import axo.core.concurrent.SchedulerContext;
import axo.core.data.ByteString;
import axo.core.producers.EmptyProducer;
import axo.core.producers.IterableProducer;
import axo.core.producers.ProducerWrapper;
import axo.core.producers.PublisherWrapper;
import axo.core.storage.MapDbStorage;
import axo.core.storage.Storage;

public final class StreamContext implements ProducerFactory, AutoCloseable {
	private final StreamContext parent;
	private final SchedulerContext schedulerContext;
	private @Deprecated final StreamExecutorFactory subscriptionFactory;
	private final Storage storage;
	
	private StreamContext (
			final Optional<StreamContext> parent, 
			final SchedulerContext schedulerContext, 
			final StreamExecutorFactory subscriptionFactory,
			final Function<StreamContext, Storage> storageFactory) {
		
		if (subscriptionFactory == null) {
			throw new NullPointerException ("subscriptionFactory cannot be null");
		}

		this.parent = Objects.requireNonNull (parent, "parent cannot be null").orElse (null);
		this.schedulerContext = Objects.requireNonNull (schedulerContext, "schedulerContext cannot be null");
		this.subscriptionFactory = Objects.requireNonNull (subscriptionFactory, "subscriptionFactory cannot be null");
		this.storage = Objects
			.requireNonNull (storageFactory, "storageFactory cannot be null")
			.apply (this);
	}
	
	public static StreamContext create (final Scheduler scheduler, final StreamExecutorFactory factory) {
		return new StreamContext (
				Optional.empty (), 
				scheduler, 
				factory,
				(context) -> new MapDbStorage (context));
	}
	
	public static StreamContext create (
			final Scheduler scheduler, 
			final StreamExecutorFactory factory,
			final Function<StreamContext, Storage> storageFactory) {
		return new StreamContext (
				Optional.empty (), 
				scheduler, 
				factory,
				storageFactory);
	}
	
	@Override
	public void close () throws Exception {
		storage.close ();
	}
	
	@Deprecated
	public StreamExecutorFactory getSubscriptionFactory () {
		return subscriptionFactory;
	}

	public Optional<StreamContext> getParent () {
		return Optional.ofNullable (parent);
	}
	
	public SchedulerContext getScheduler () {
		return schedulerContext;
	}
	
	public Storage getStorage () {
		return storage;
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
						return i < max;
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
						return i < max;
					}

					@Override
					public Long next () {
						return i ++;
					}
				};
			}
		});
	}
	
	@Override
	public <E> Producer<E> empty () {
		return new EmptyProducer<E> (this);
	}

	@Override
	public Producer<ByteString> from (final ByteString bs, final int blockSize) {
		return from (
				Objects.requireNonNull (bs, "bs cannot be null")
					.partition (blockSize)
			);
	}
}
