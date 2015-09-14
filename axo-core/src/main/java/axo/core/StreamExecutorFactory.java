package axo.core;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Deprecated
public interface StreamExecutorFactory {

	/**
	 * Creates a new immediate subscription. An immediate subscription is used when the
	 * producer can produce the elements on demand without blocking or waiting.
	 * For example when reading from a collection, array or range of integers.
	 * 
	 * Takes a subscriber that will receive the elements and a producer function
	 * that can produce the elements in the producer on demand.
	 * 
	 * The function receives a long parameter, indicating the number of elements that
	 * must be produced. Returns true if there are more items in the producer, false
	 * otherwise.
	 *  
	 * @param subscriber		The subscriber to produce elements to.
	 * @param producerFunction	The function that produces the elements.
	 * @return					An immediate subscription.
	 */
	<T> ImmediateExecutor createImmediateExecutor (Subscriber<? super T> subscriber, Function2<Subscriber<? super T>, Long, Boolean> producerFunction);
	
	StreamExecutor createStreamExecutor ();
	
	/**
	 * Executor for producers that have can produce elements immediately.
	 * Used for ranges, java collections, etc. Producers where values
	 * are either calculated or buffered
	 */
	public interface ImmediateExecutor extends Subscription {
	}
}
