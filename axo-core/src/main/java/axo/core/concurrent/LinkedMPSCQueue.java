package axo.core.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class LinkedMPSCQueue<T> {
	private AtomicReference<Node<T>> head;
	private AtomicReference<Node<T>> tail;

	public LinkedMPSCQueue () {
		final Node<T> emptyNode = new Node<> (null);
		
		this.head = new AtomicReference<> (emptyNode);
		this.tail = new AtomicReference<> (emptyNode);
	}
	
	public void offer (final T value) {
		final Node<T> node = new Node<> (Objects.requireNonNull (value, "value cannot be null"));

		// Compare and set the tail next pointer, if the next pointer has
		// been updated, traverse the next pointer until the last node in
		// the chain has been updated:
		Node<T> previousTail = tail.get ();
		while (!previousTail.next.compareAndSet (null, node)) {
			previousTail = previousTail.next.get ();
		}
		
		// Atomically update the tail node:
		tail.set (node);
	}
	
	public T take () {
		// Atomically update the head node along:
		Node<T> previousHead;
		Node<T> newHead;
		
		do {
			previousHead = head.get ();
			newHead = previousHead.next.get ();
		} while (newHead != null && !head.compareAndSet (previousHead, newHead));
		
		// The result is either null, or the value stored in the new list head:
		if (newHead == null) {
			return null;
		}
		
		// Clear the value in the new head node. It is no longer part of the
		// queue and can be garbage collected:
		final T result = newHead.value;
		
		newHead.value = null;
		
		return result;
	}
	
	public T peek () {
		Node<T> currentHead = head.get ();
		Node<T> nextHead = currentHead.next.get ();
		
		return nextHead == null ? null : nextHead.value;
	}
	
	private final static class Node<T> {
		public volatile T value;
		public final AtomicReference<Node<T>> next = new AtomicReference<> (null);
		
		public Node (final T value) {
			this.value = value;
		}
	}

}
