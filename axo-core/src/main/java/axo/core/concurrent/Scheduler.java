package axo.core.concurrent;

import java.util.concurrent.TimeUnit;

public interface Scheduler extends SchedulerContext {
	void stop (long timeout, TimeUnit unit) throws InterruptedException;
}