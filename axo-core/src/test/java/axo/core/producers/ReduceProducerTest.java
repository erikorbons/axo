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
import axo.core.executors.ExecutorServiceExecutor;

public class ReduceProducerTest extends PublisherVerification<Long> {
	private static ExecutorServiceExecutor streamExecutor;
	private static StreamContext streamContext;
	
	public ReduceProducerTest () {
		super (new TestEnvironment (300l), 1000l);
	}
	
	@BeforeClass
	public static void createContext () {
		final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor (1, 4, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable> ());
		streamExecutor = new ExecutorServiceExecutor (threadPoolExecutor);
		streamContext = new StreamContext (streamExecutor);
	}
	
	@AfterClass
	public static void deleteContext () throws Throwable {
		streamExecutor.shutdown ();
		streamContext = null;
	}
	
	@Override
	public Publisher<Long> createFailedPublisher () {
		return null;
	}

	@Override
	public Publisher<Long> createPublisher (final long n) {
		if (n == 0) {
			return streamContext
				.range (0l, 0l)
				.reduce ((a, b) -> a + b);
		}
		
		return streamContext
			.range (0l, 1000l + (long)(Math.random () * 100.0))
			.reduce ((a, b) -> a + b);
	}
	
	@Override
	public long maxElementsFromPublisher() {
		return 1;
	}
	
	@Test
	public void test () {
	}
}
