package axo.core;

import java.util.LinkedList;
import java.util.Queue;

import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.StreamExecutor.SerialScheduler;

public abstract class AbstractProcessor<T, R> implements Processor<T, R> {

	private final SerialScheduler scheduler;
	
	private Subscription sourceSubscription;
	private Subscriber<? super R> targetSubscriber;
	
	public static enum ProcessorState {
		WORKING,
		IDLE,
		NEEDS_INPUT,
		FINISHED
	}
	
	public static class ProcessResult {
		private final long requestCount;
		private final ProcessorState state;

		public ProcessResult (final long n) {
			this (ProcessorState.NEEDS_INPUT, n);
		}
		
		public ProcessResult (final ProcessorState state) {
			this (state, 0);
		}
		
		public ProcessResult (final ProcessorState state, final long requestCount) {
			if (state == null) {
				throw new NullPointerException ("state cannot be null");
			}
			if (requestCount < 0) {
				throw new IllegalArgumentException ("requestCount must be >= 0"); 
			}
			if (ProcessorState.NEEDS_INPUT.equals (state) && requestCount == 0) {
				throw new IllegalArgumentException ("Should request at least one element if state is NEEDS_INPUT");
			}
			
			this.state = state;
			this.requestCount = requestCount;
		}
		
		public long getRequestCount () {
			return requestCount;
		}
		
		public ProcessorState getState () {
			return state;
		}
	}

	public AbstractProcessor (final StreamExecutor executor) {
		if (executor == null) {
			throw new NullPointerException ("executor cannot be null");
		}
		
		this.scheduler = executor.createSerialScheduler ();
	}
	
	/**
	 * Stores the error that has been received from the upstream subscription.
	 */
	private Throwable sourceError = null;

	/**
	 * Indicates that the source has completed normally (without error).
	 */
	private boolean sourceCompleted = false;
	
	/**
	 * List of elements that have been received from the source and that
	 * haven't been processed yet.
	 */
	private Queue<T> sourceElements = new LinkedList<> ();
	
	/**
	 * The number of elements that have been requested from the source.
	 */
	private long sourceRequested = 0;
	
	/**
	 * Set to true if the target requested a cancel.
	 */
	private boolean targetRequestedCancel = false; 
	
	/**
	 * The number of elements that have been requested by the target.
	 */
	private long targetRequested = 0;
	
	/**
	 * List containing elements that are ready to be sent to the target.
	 */
	private Queue<R> targetElements = new LinkedList<> ();
	
	/**
	 * Set to true when the process method indicated the processor should process
	 * no more elements.
	 */
	private boolean processorFinished = false;
	
	@Override
	public void onComplete () {
		schedule (() -> sourceCompleted = true);
	}

	@Override
	public void onError (final Throwable error) {
		schedule (() -> sourceError = error);
	}

	@Override
	public void onNext (final T t) {
		schedule (() -> { sourceElements.add (t); -- sourceRequested; });
	}

	@Override
	public void onSubscribe (final Subscription subscription) {
		// Cancel immediately if an exception has been raised within this
		// processor while waiting for onSubscribe:
		if (sourceError != null) {
			subscription.cancel ();
		}
		
		sourceSubscription = subscription;
	}
	
	/**
	 * Invoked by this processor to send an element to the target.
	 * 
	 * @param element
	 */
	public void produce (final R element) {
		targetElements.add (element);
	}

	/**
	 * Schedules an action to be performed by this processor in the future.
	 * Scheduled actions never overlap previously scheduled actions and
	 * are always executed in order.
	 * 
	 * Exceptions raised by actions lead to onError on the target subscriber
	 * and terminate stream processing.
	 *  
	 * @param action
	 */
	public void schedule (final Action0 action) {
		scheduler.scheduleAction (() -> {
			try {
				action.apply ();
				doWork ();
			} catch (Throwable t) {
				// Report the exception and disable this processor:
				if (targetSubscriber != null) {
					targetSubscriber.onError (t);
				} else {
					// Store the exception for when a subscriber subscribes
					// to this processor:
					sourceError = t;
				}
		
				if (sourceSubscription != null) {
					sourceSubscription.cancel ();
				}
				
				targetSubscriber = null;
				sourceSubscription = null;
				return;
			}
		});
	}
	
	private void doWork () throws Throwable {
		if (sourceSubscription == null || targetSubscriber == null) {
			// Do nothing if the processor is not fully connected:
			return;
		}
		
		// Handle errors from the source:
		if (sourceError != null) {
			// Propagate the error:
			terminate ();
			targetSubscriber.onError (sourceError);
			sourceSubscription = null;
			targetSubscriber = null;
			return;
		}
		
		// Handle cancel requests:
		if (targetRequestedCancel) {
			terminate ();
			sourceSubscription.cancel ();
			sourceSubscription = null;
			targetSubscriber = null;
			return;
		}
		
		// Produce elements to the target:
		if (targetRequested > 0 && !processorFinished) {
			try {
				final ProcessResult result = process (sourceElements, targetRequested - targetElements.size (), sourceCompleted);
				final long n = result.getRequestCount ();
				final ProcessorState state = result.getState ();
				
				if (n > sourceRequested && !sourceCompleted) {
					sourceSubscription.request (n - sourceRequested);
					sourceRequested = n;
				}
				
				if (ProcessorState.FINISHED.equals (state)) {
					processorFinished = true;
				}
			} catch (Throwable t) {
				// Report the exception and disable this processor:
				targetSubscriber.onError (t);
				sourceSubscription.cancel ();
				targetSubscriber = null;
				sourceSubscription = null;
				return;
			}
		}
		
		// Flush items that have been previously produced to the target:
		while (targetRequested > 0 && !targetElements.isEmpty ()) {
			targetSubscriber.onNext (targetElements.poll ());
			-- targetRequested;
		}
		
		// Stop the processor if the source has completed:
		if (processorFinished && targetElements.isEmpty () && targetRequested > 0) {
			terminate ();
			targetSubscriber.onComplete ();
			targetSubscriber = null;
			sourceSubscription = null;
		}
	}
	
	/**
	 * Worker method that processes input elements.
	 * 
	 * @param input	The input queue to take elements from.
	 * @param requestCount		The number of elements that have been requested on this processor.
	 * 							This value can be used as a guideline when producing new items,
	 * 							however the processor is permitted to produce less than or more
	 * 							than this amount.
	 * @param sourceExhausted	True if the source has completed: there will never be more items 
	 * 							added to the input queue in this case. 
	 * @return	The number of elements that must be fetched from the source
	 * 			before more elements can be produced by this processor.
	 */
	public abstract ProcessResult process (Queue<T> input, long requestCount, boolean sourceExhausted);

	/**
	 * Invoked when this processor terminates.
	 */
	public abstract void terminate ();
	
	@Override
	public void subscribe (final Subscriber<? super R> targetSubscriber) {
		if (this.targetSubscriber != null) {
			targetSubscriber.onError (new IllegalStateException ("Multi subscribe on a processor of type `" + getClass ().getCanonicalName () + "' not supported."));
			return;
		}
		
		if (sourceError != null) {
			targetSubscriber.onError (sourceError);
			return;
		}
		
		this.targetSubscriber = targetSubscriber;
		
		targetSubscriber.onSubscribe (new Subscription () {
			@Override
			public void request (final long count) {
				schedule (new Action0 () {
					@Override
					public void apply() {
						if (count <= 0) {
							throw new IllegalArgumentException ("count must be > 0 (reactive streams 3.9)");
						}
						
						if (targetRequested + count < 0) {
							targetRequested = Long.MAX_VALUE;
						} else {
							targetRequested += count;
						}
					}
				});
			}
			
			@Override
			public void cancel () {
				schedule (() -> targetRequestedCancel = true);
			}
		});
	}
}
