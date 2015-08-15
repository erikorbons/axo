package axo.core.concurrent;

import java.util.Optional;

import axo.core.Action0;

public interface SchedulerContext {
	Optional<SchedulerContext> getParent ();
	boolean isSynchronized ();
	
	String getName ();
	SchedulerContext createContext ();
	SchedulerContext createContext (String name);
	SchedulerContext createSynchronizedContext ();
	SchedulerContext createSynchronizedContext (String name);
	
	void schedule (Action0 action);
}
