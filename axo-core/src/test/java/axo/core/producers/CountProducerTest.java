package axo.core.producers;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import axo.core.StreamContext;
import axo.core.executors.ExecutorServiceExecutor;

public class CountProducerTest extends PublisherVerification<Long> {

	private static ExecutorServiceExecutor streamExecutor;
	private static StreamContext streamContext;
	
	public CountProducerTest () {
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
	public Publisher<Long> createErrorStatePublisher () {
		return null;
	}

	@Override
	public Publisher<Long> createPublisher (final long n) {
		if (n == 0) {
			return null;
		}
		
		return streamContext
			.range (0l, 1000l + (long)(Math.random () * 100.0))
			.count ();
	}
	
	@Override
	public long maxElementsFromPublisher() {
		return 1;
	}
	
	@Override @Test
	public void required_spec109_mustIssueOnSubscribeForNonNullSubscriber() throws Throwable {
		throw new SkipException ("Unable to run this test. The original test uses a stream of length 0, but CountProducer doesn't support empty streams");
	}
}
