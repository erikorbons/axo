package axo.core.operators;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.Stack;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import axo.core.Action0;
import axo.core.Action1;
import axo.core.Operator;
import axo.core.StreamContext;
import axo.core.concurrent.SchedulerContext;

public abstract class FsmOperator<T, R> extends Operator<T, R> {

	private final SchedulerContext scheduler;
	private Subscription subscription = null;
	private boolean completed = false;
	private boolean terminated = false;
	private Stack<State<T>> states = new Stack<> ();
	private Queue<R> producedElements = new LinkedList<> ();
	private long requestedCount = 0;
	private boolean elementRequested = false;
	private boolean submitScheduled = false;
	
	public FsmOperator (final StreamContext context, final Subscriber<? super R> subscriber) {
		super(context, subscriber);
		
		this.scheduler = context.getScheduler().createSynchronizedContext ();
		
		states.push (state (this::handleInput, this::handleComplete));
	}

	@Override
	public final void onSubscribe (final Subscription subscription) {
		this.subscription = subscription;
		
		getSubscriber ().onSubscribe (new Subscription () {
			@Override
			public void request (final long n) {
				if (n <= 0) {
					getSubscriber ().onError (new IllegalArgumentException ("n should be > 0 3.9"));
					terminated = true;
					return;
				}
				
				scheduler.schedule (() -> requestElements (n));
			}
			
			@Override
			public void cancel() {
				scheduler.schedule (() -> cancelSubscription ());
			}
		});
		
		subscription.request (1);
		elementRequested = true;
	}
	
	@Override
	public final void onComplete () {
		scheduler.schedule (this::handleOnComplete);
	}
	
	@Override
	public final void onError (final Throwable t) {
		scheduler.schedule (() -> handleOnError (t));
	}
	
	@Override
	public final void onNext (final T t) {
		scheduler.schedule (() -> handleOnNext (t));
	}

	private void handleOnComplete () {
		if (terminated) {
			return;
		}
		
		wrapExceptions (() -> {
			states.peek ().getCompletionHandler ().apply ();
		});
	}
	
	private void handleOnError (final Throwable t) {
		getSubscriber ().onError (t);
		terminated = true;
	}
	
	private void handleOnNext (final T t) {
		elementRequested = false;
		
		if (terminated) {
			return;
		}
		
		wrapExceptions (() -> {
			states.peek ().getInputHandler().apply (t);
		});
		
		if (!terminated && !completed && requestedCount > 0 && producedElements.isEmpty ()) {
			subscription.request (1);
			elementRequested = true;
		}
	}
	
	private void requestElements (final long n) {
		if (terminated) {
			return;
		}
		
		if (requestedCount + n <= requestedCount) {
			requestedCount = Long.MAX_VALUE;
		} else {
			requestedCount += n;
		}
		
		if (!elementRequested) {
			elementRequested = true;
			subscription.request (1);
		}
		
		if (!submitScheduled) {
			scheduler.schedule (this::submitProducedElements);
			submitScheduled = true;
		}
	}
	
	private void cancelSubscription () {
		if (terminated) {
			return;
		}
		
		subscription.cancel ();
		terminated = true;
	}
	
	private void wrapExceptions (final Action0 action) {
		try {
			action.apply ();
		} catch (Throwable t) {
			getSubscriber().onError (t);
			terminated = true;
			if (subscription != null) {
				subscription.cancel ();
			}
		}
	}
	
	private void submitProducedElements () {
		submitScheduled = false;
		
		wrapExceptions (() -> {
			while (requestedCount > 0 && !producedElements.isEmpty ()) {
				getSubscriber ().onNext (producedElements.remove ());
				-- requestedCount;
			}
			
			if (completed && producedElements.isEmpty ()) {
				getSubscriber ().onComplete ();
			}
			
			if (!elementRequested && !terminated && !completed && requestedCount > 0 && producedElements.isEmpty ()) {
				subscription.request (1);
				elementRequested = true;
			}
		});
	}
	
	protected final void produce (final R element) {
		if (completed) {
			throw new IllegalStateException ("Cannot produce elements when the in the completed state");
		}
		
		producedElements.add (Objects.requireNonNull (element, "element cannot be null"));
		if (!submitScheduled) {
			scheduler.schedule (this::submitProducedElements);
			submitScheduled = true;
		}
	}
	
	protected final void complete () {
		completed = true;
		if (!submitScheduled) {
			scheduler.schedule (this::submitProducedElements);
			submitScheduled = true;
		}
	}

	@SafeVarargs
	protected final void gotoState (final State<T> state, final T ... ts) {
		Objects.requireNonNull (state, "state cannot be null");
		
		states.pop ();
		states.push (state);
		
		wrapExceptions (() -> {
			for (final T t: ts) {
				states.peek ().getInputHandler ().apply (t);
			}
		});
	}
	
	@SafeVarargs
	protected final void pushState (final State<T> state, final T ... ts) {
		Objects.requireNonNull (state, "state cannot be null");
		
		states.push (state);
		
		wrapExceptions (() -> {
			for (final T t: ts) {
				states.peek ().getInputHandler ().apply (t);
			}
		});
	}
	
	@SafeVarargs
	protected final void popState (final T ... ts) {
		if (states.size () <= 1) {
			throw new IllegalStateException ("cannot pop the last state");
		}
		
		states.pop ();
		
		wrapExceptions (() -> {
			for (final T t: ts) {
				states.peek ().getInputHandler ().apply (t);
			}
		});
	}
	
	protected final void resetState () {
		states.clear ();
		states.push (new State<T> (this::handleInput, this::handleComplete));
	}
	
	public abstract void handleInput (final T input);
	public abstract void handleComplete ();
	
	public static <T> State<T> state (final Action1<T> handleInput, final Action0 handleComplete) {
		return new State<> (handleInput, handleComplete);
	}
	
	public final static class State<T> {
		private final Action1<T> handleInput;
		private final Action0 handleComplete;
		
		public State (final Action1<T> handleInput, final Action0 handleComplete) {
			this.handleInput = Objects.requireNonNull (handleInput, "handleInput cannot be null");
			this.handleComplete = Objects.requireNonNull (handleComplete, "handleComplete cannot be null");
		}
		
		public Action1<T> getInputHandler () {
			return handleInput;
		}
		
		public Action0 getCompletionHandler () {
			return handleComplete;
		}
	}
}
