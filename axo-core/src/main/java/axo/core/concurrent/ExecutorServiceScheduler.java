package axo.core.concurrent;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import axo.core.Action0;

public final class ExecutorServiceScheduler implements Scheduler {

	private final String name;
	private final ExecutorService executorService;
	private final AtomicLong childCount = new AtomicLong (0);
	
	public ExecutorServiceScheduler (final String name, final ExecutorService executorService) {
		this.name = Objects.requireNonNull (name, "name cannot be null");
		this.executorService = Objects.requireNonNull (executorService, "executorService cannot be null");
	}
	
	@Override
	public boolean isSynchronized () {
		return false;
	}
	
	@Override
	public Optional<SchedulerContext> getParent () {
		return Optional.empty ();
	}

	@Override
	public String getName () {
		return name;
	}

	@Override
	public SchedulerContext createContext () {
		return createContext ("context-" + childCount.getAndIncrement ());
	}

	@Override
	public SchedulerContext createContext (final String name) {
		return new Context (
				Objects.requireNonNull (name, "name cannot be null"),
				this,
				this
			);
	}
	
	@Override
	public SchedulerContext createSynchronizedContext () {
		return createSynchronizedContext ("context-sync-" + childCount.getAndIncrement ());
	}

	@Override
	public SchedulerContext createSynchronizedContext (final String name) {
		return new SynchronizedContext (
				Objects.requireNonNull (name, "name cannot be null"),
				this,
				this
			);
	}
	
	
	@Override
	public void schedule (final Action0 action) {
		Objects.requireNonNull (action, "action cannot be null");
		
		executorService.execute (() -> {
			action.apply ();	
		});
	}
	
	@Override
	public void stop (long timeout, TimeUnit unit) throws InterruptedException {
		executorService.shutdown ();
		executorService.awaitTermination (timeout, unit);
	}

	private abstract static class ContextBase implements SchedulerContext {
		private final String name;
		private final ExecutorServiceScheduler scheduler;
		private final SchedulerContext parent;
		private final AtomicLong childCount = new AtomicLong (0);
		
		public ContextBase (final String name, final SchedulerContext parent, final ExecutorServiceScheduler scheduler) {
			this.parent = Objects.requireNonNull (parent, "parent cannot be null");
			this.scheduler = Objects.requireNonNull (scheduler, "scheduler cannot be null");
			
			this.name = parent.getName () 
					+ "/" 
					+ Objects.requireNonNull (name, "name cannot be null");
		}

		@Override
		public Optional<SchedulerContext> getParent () {
			return Optional.of (parent);
		}

		@Override
		public String getName () {
			return name;
		}

		@Override
		public SchedulerContext createContext () {
			return createContext ("context-" + childCount.getAndIncrement ());
		}

		@Override
		public SchedulerContext createContext (final String name) {
			return new Context (
					Objects.requireNonNull (name, "name cannot be null"),
					this,
					scheduler
				);
		}
		
		@Override
		public SchedulerContext createSynchronizedContext () {
			return createContext ("context-sync-" + childCount.getAndIncrement ());
		}
		
		@Override
		public SchedulerContext createSynchronizedContext (final String name) {
			return new SynchronizedContext (
					Objects.requireNonNull (name, "name cannot be null"),
					this,
					scheduler
				);
		}
	}
	
	private final static class Context extends ContextBase {
		public Context (final String name, final SchedulerContext parent, final ExecutorServiceScheduler scheduler) {
			super (name, parent, scheduler);
		}
		
		@Override
		public boolean isSynchronized () {
			return false;
		}
		
		@Override
		public void schedule (final Action0 action) {
			// Unsynchronized schedulers can simply delegate to the parent:
			getParent ().get ().schedule (Objects.requireNonNull (action, "action cannot be null"));
		}
	}
	
	private final static class SynchronizedContext extends ContextBase {
		private final LinkedMPSCQueue<Action0> scheduledActions = new LinkedMPSCQueue<> ();
		private AtomicBoolean executionScheduled = new AtomicBoolean (false); 
		
		public SynchronizedContext (final String name, final SchedulerContext parent, final ExecutorServiceScheduler scheduler) {
			super (name, parent, scheduler);
		}
		
		@Override
		public boolean isSynchronized () {
			return true;
		}
		
		@Override
		public void schedule (final Action0 action) {
			// Add an entry to the scheduled actions:
			scheduledActions.offer (Objects.requireNonNull (action, "action cannot be null"));
			
			// There is at least one element in the actions queue, schedule execution
			// of the drain operation:
			if (executionScheduled.compareAndSet (false, true)) {
				getParent ().get ().schedule (this::drain);
			}
		}
		
		private void drain () {
			for (int i = 0; i < 100; ++ i) {
				final Action0 action = scheduledActions.take ();
				if (action == null) {
					break;
				}
				
				action.apply ();
			}
			
			executionScheduled.set (false);
			
			// If there are still items in the action queue, schedule another execution:
			if (scheduledActions.peek () != null && executionScheduled.compareAndSet (false, true)) {
				getParent ().get ().schedule (this::drain);
			}
		}
	}
}
