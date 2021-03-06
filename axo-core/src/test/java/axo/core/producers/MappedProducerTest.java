package axo.core.producers;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import axo.core.StreamContext;
import axo.core.concurrent.ExecutorServiceScheduler;
import axo.core.concurrent.Scheduler;
import axo.core.executors.ExecutorServiceExecutor;

public class MappedProducerTest extends PublisherVerification<Double> {

	private static Scheduler scheduler;
	private static ExecutorServiceExecutor streamExecutor;
	private static StreamContext streamContext;
	
	public MappedProducerTest() {
		super (new TestEnvironment (300l), 1000l);
	}
	
	@BeforeClass
	public static void createContext () {
		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor (1, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable> ());
		scheduler = new ExecutorServiceScheduler ("scheduler", threadPoolExecutor);
		streamExecutor = new ExecutorServiceExecutor (threadPoolExecutor);
		streamContext = StreamContext.create (scheduler, streamExecutor);
	}
	
	@AfterClass
	public static void deleteContext () throws Throwable {
		streamExecutor.shutdown ();
		scheduler.stop (1000, TimeUnit.MILLISECONDS);
		streamContext = null;
	}

	@Override
	public Publisher<Double> createFailedPublisher () {
		return null;
	}

	@Override
	public Publisher<Double> createPublisher (final long n) {
		return streamContext
			.range (0, n)
			.map ((t) -> 2.0 * t);
	}
	
	@Test
	public void f() {
	}
}
