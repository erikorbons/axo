package axo.core.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import axo.core.Action1;
import axo.core.Function;

import static org.testng.Assert.*;

public class ExecutorServiceSchedulerTest {
	@Test (invocationCount = 100, successPercentage = 100, singleThreaded = true)
	public void testExecution () throws Throwable {
		final ConcurrentMap<Integer, Boolean> executed = new ConcurrentHashMap<> ();
		
		withScheduler (scheduler -> {
			for (int i = 0; i < 1000; ++ i) {
				final int value = i;
				scheduler.schedule (() -> executed.put (value, true));
			}
		});
		
		for (int i = 0; i < 1000; ++ i) {
			assertTrue (executed.containsKey (i), "Action " + i + " has not been executed");
		}
	}
	
	@Test (invocationCount = 100, successPercentage = 100, singleThreaded = true)
	public void testExecutionContext () throws Throwable {
		final ConcurrentMap<Integer, Boolean> executed = new ConcurrentHashMap<> ();
		
		withScheduler (scheduler -> {
			final SchedulerContext context = scheduler.createContext ();
			for (int i = 0; i < 1000; ++ i) {
				final int value = i;
				context.schedule (() -> {
					executed.put (value, true);
				});
			}
		});
		
		for (int i = 0; i < 1000; ++ i) {
			assertTrue (executed.containsKey (i), "Action " + i + " has not been executed");
		}
	}
	
	@Test (invocationCount = 500, successPercentage = 100, singleThreaded = true)
	public void testExecutionSynchronizedContext () throws Throwable {
		final ConcurrentMap<Integer, Boolean> executed = new ConcurrentHashMap<> ();
		final ConcurrentLinkedQueue<Integer> receivedValues = new ConcurrentLinkedQueue<> ();
		
		withScheduler (scheduler -> {
			final SchedulerContext context = scheduler.createSynchronizedContext ();
			for (int i = 0; i < 1000; ++ i) {
				final int value = i;
				context.schedule (() -> {
					executed.put (value, true);
					receivedValues.add (value);
					if (Math.random () < .3) {
						Thread.yield ();
					}
				});
			}
			
			final CompletableFuture<Void> future = new CompletableFuture<> ();
			context.schedule (() -> future.complete (null));
			return future;
		});
		
		for (int i = 0; i < 1000; ++ i) {
			assertTrue (executed.containsKey (i), "Action " + i + " has not been executed");
			assertEquals (receivedValues.poll ().intValue (), i); 
		}
	}
	
	@Test (invocationCount = 500, successPercentage = 100, singleThreaded = true)
	public void testExecutionSynchronizedContextStacked () throws Throwable {
		final ConcurrentMap<Integer, Boolean> executed = new ConcurrentHashMap<> ();
		final ConcurrentLinkedQueue<Integer> receivedValues = new ConcurrentLinkedQueue<> ();
		
		withScheduler (scheduler -> {
			final SchedulerContext baseContext = scheduler.createSynchronizedContext ();
			final SchedulerContext context = baseContext.createSynchronizedContext ();
			int i;
			
			for (i = 0; i < 1000; ++ i) {
				final int value = i;
				context.schedule (() -> {
					executed.put (value, true);
					receivedValues.add (value);
					if (Math.random () < .3) {
						Thread.yield ();
					}
				});
			}
			
			final CompletableFuture<Void> future = new CompletableFuture<> ();
			context.schedule (() -> future.complete (null));
			return future;
		});
		
		for (int i = 0; i < 1000; ++ i) {
			assertTrue (executed.containsKey (i), "Action " + i + " has not been executed");
			assertEquals (receivedValues.poll ().intValue (), i); 
		}
	}
	
	private void withScheduler (final Action1<ExecutorServiceScheduler> action) throws Throwable {
		final ExecutorService executorService = new ThreadPoolExecutor (2, 4, 500, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<> (1000));
		final ExecutorServiceScheduler scheduler = new ExecutorServiceScheduler ("scheduler", executorService);

		action.apply (scheduler);
		
		scheduler.stop (1, TimeUnit.SECONDS);
	}

	private void withScheduler (final Function<ExecutorServiceScheduler, CompletableFuture<Void>> action) throws Throwable {
		final ExecutorService executorService = new ThreadPoolExecutor (2, 4, 500, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<> (1000));
		final ExecutorServiceScheduler scheduler = new ExecutorServiceScheduler ("scheduler", executorService);
		final CompletableFuture<Void> future = action.apply (scheduler);
		
		future.get (2, TimeUnit.SECONDS);
		
		scheduler.stop (1, TimeUnit.SECONDS);
	}
}
