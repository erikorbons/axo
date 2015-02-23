package axo.core;

import java.util.Comparator;
import java.util.List;

import org.reactivestreams.Publisher;

import axo.core.producers.BufferProducer;
import axo.core.producers.CountProducer;
import axo.core.producers.FlattenProducer;
import axo.core.producers.MappedProducer;
import axo.core.producers.ReduceProducer;
import axo.core.producers.SkipProducer;
import axo.core.producers.TakeProducer;

public abstract class Producer<T> implements Publisher<T>, ProducerFactory {

	public abstract StreamContext getContext ();
	
	@Override
	@SafeVarargs
	public final <E> Producer<E> from (final E ... ts) {
		return getContext ().from (ts);
	}
	
	@Override
	public final <E> Producer<E> from (final Iterable<E> ts) {
		return getContext ().from (ts);
	}
	
	@Override
	public final <E> Producer<E> from (final Publisher<E> ts) {
		return getContext ().from (ts);
	}
	
	@Override
	public final <E> Producer<E> from (final Producer<E> ts) {
		return getContext ().from (ts);
	}
	
	@Override
	public final Producer<Integer> range (final int min, final int max) {
		return getContext ().range (min, max);
	}
	
	@Override
	public final Producer<Long> range (final long min, final long max) {
		return getContext ().range (min, max);
	}
	
	public <R> Producer<R> map (final Function<? super T, ? extends R> mapper) {
		return new MappedProducer<> (this, mapper);
	}
	
	public <R> Producer<R> flatMap (final Function<? super T, ? extends Producer<? extends R>> mapper) {
		return flatten (map (mapper));
	}
	
	public static <R> Producer<R> flatten (final Producer<Producer<? extends R>> source) {
		return new FlattenProducer<> (source, 1);
	}
	
	public Producer<T> head () {
		return take (1);
	}
	
	public Producer<T> tail () {
		return skip (1);
	}
	
	public Producer<T> skip (final long n) {
		if (n == 0) {
			return this;
		}
		
		return new SkipProducer<> (this, n);
	}
	
	public Producer<T> take (final long n) {
		return new TakeProducer<> (this, n);
	}
	
	public Producer<Boolean> allMatch (final Function<? super T, Boolean> predicate) {
		if (predicate == null) {
			throw new NullPointerException ("predicate cannot be null");
		}
		
		return map (predicate)
			.reduce ((a, b) -> a && b);
	}

	public Producer<Boolean> anyMatch (final Function<? super T, Boolean> predicate) {
		if (predicate == null) {
			throw new NullPointerException ("predicate cannot be null");
		}
		
		return map (predicate)
			.reduce ((a, b) -> a || b);
	}
	
	public Producer<Boolean> noneMatch (final Function<? super T, Boolean> predicate) {
		if (predicate == null) {
			throw new NullPointerException ("predicate cannot be null");
		}
		
		return anyMatch (predicate)
			.map ((v) -> !v);
	}
	
	public Producer<Long> count () {
		return new CountProducer (this);
	}
	
	public Producer<T> filter (final Function<T, Boolean> fn) {
		return null;
	}
	
	public <E> Producer<E> empty () {
		return getContext ().<E>empty ();
	}
	
	public Producer<T> max (final Comparator<? super T> comparator) {
		if (comparator == null) {
			throw new NullPointerException ("comparator cannot be null");
		}
		
		return reduce ((a, b) -> comparator.compare (a, b) > 0 ? a : b);
	}
	
	public Producer<T> min (final Comparator<? super T> comparator) {
		if (comparator == null) {
			throw new NullPointerException ("comparator cannot be null");
		}
		
		return reduce ((a, b) -> comparator.compare (a, b) < 0 ? a : b);
	}

	public Producer<T> reduce (final Function2<? super T, ? super T, ? extends T> reducer) {
		return new ReduceProducer<> (this, reducer);
	}

	public <B, R> Producer<R> zip (final Producer<B> b, final Function2<? super T, ? super B, ? extends R> zipper) {
		return null;
	}
	
	public Producer<List<T>> buffer (final int bufferSize) {
		return new BufferProducer<T> (this, bufferSize);
	}
}
