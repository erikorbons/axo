package axo.core;

import java.util.Comparator;

import org.reactivestreams.Publisher;

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
		return null;
	}
	
	public <R> Producer<R> flatMap (final Function<? super T, ? extends Producer<? extends R>> mapper) {
		return null;
	}
	
	public Producer<T> head () {
		return null;
	}
	
	public Producer<T> tail () {
		return null;
	}
	
	public Producer<T> skip (final long n) {
		return null;
	}
	
	public Producer<T> take (final long n) {
		return null;
	}
	
	public Producer<Boolean> allMatch (final Function<? super T, Boolean> predicate) {
		return null;
	}

	public Producer<Boolean> anyMatch (final Function<? super T, Boolean> predicate) {
		return null;
	}
	
	public Producer<Boolean> noneMatch (final Function<? super T, Boolean> predicate) {
		return null;
	}
	
	public Producer<Long> count () {
		return null;
	}
	
	public Producer<T> filter (final Function<T, Boolean> fn) {
		return null;
	}
	
	static <T> Producer<T> empty () {
		return null;
	}
	
	public Producer<T> max (final Comparator<? super T> comparator) {
		return null;
	}
	
	public Producer<T> min (final Comparator<? super T> comparator) {
		return null;
	}

	public Producer<T> reduce (final Function2<? super T, ? super T, ? extends T> reducer) {
		return null;
	}

	public <B, R> Producer<R> zip (final Producer<B> b, final Function2<? super T, ? super B, ? extends R> zipper) {
		return null;
	}
}
