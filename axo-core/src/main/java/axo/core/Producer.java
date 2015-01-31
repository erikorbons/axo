package axo.core;

import java.util.Collection;
import java.util.Comparator;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class Producer<T> implements Publisher<T> {

	public static <T> Producer<T> from (final T ... ts) {
		return null;
	}
	
	public static <T> Producer<T> from (final Collection<T> ts) {
		return null;
	}
	
	public static <T> Producer<T> from (final Publisher<T> ts) {
		return null;
	}
	
	public static <T> Producer<T> from (final Producer<T> ts) {
		return null;
	}
	
	public static Producer<Integer> range (final int min, final int max) {
		return null;
	}
	
	public static Producer<Long> range (final long min, final long max) {
		return null;
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
	
	public void subscribe (final Subscriber<? super T> subscriber) {
		// TODO Auto-generated method stub
	}
}
