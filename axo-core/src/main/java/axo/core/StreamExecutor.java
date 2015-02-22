package axo.core;

public interface StreamExecutor {

	void start ();
	void shutdown () throws Throwable;
	void scheduleAction (Action<StreamExecutor> action);
	SerialScheduler createSerialScheduler ();
	
	public interface SerialScheduler {
		void scheduleAction (Action0 action);
	}
}
