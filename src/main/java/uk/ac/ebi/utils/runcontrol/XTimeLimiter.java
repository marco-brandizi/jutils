package uk.ac.ebi.utils.runcontrol;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import com.google.common.util.concurrent.UncheckedTimeoutException;

import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.UncheckedInterruptedException;
import uk.ac.ebi.utils.threading.ThreadUtils;

/**
 * An extension of {@link TimeLimiter} to ease the use of {@link SimpleTimeLimiter}, by offering
 * a suitable internal {@link ExecutorService} and by throwing unchecked exceptions.
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2025</dd></dl>
 *
 */
public class XTimeLimiter implements TimeLimiter
{
	private static final ExecutorService TIME_LIMITER_SERVICE = Executors.newCachedThreadPool (
		ThreadUtils.createNamingThreadFactory ( "XTimeLimiter-Thread-" )
	);

	private final TimeLimiter delegate;
	
	private XTimeLimiter ( ExecutorService executorService )
	{
		if ( executorService == null ) executorService = TIME_LIMITER_SERVICE;
		this.delegate = SimpleTimeLimiter.create ( executorService );
	}

  public static XTimeLimiter create ( ExecutorService executorService ) {
    return new XTimeLimiter ( executorService );
  }

  /**
   * Uses a default {@link ExecutorService}
   */
  public static XTimeLimiter create () {
    return new XTimeLimiter ( null );
  }

  
	@Override
	public <T> T newProxy ( T target, Class<T> interfaceType, long timeoutDuration, TimeUnit timeoutUnit )
	{
		return delegate.newProxy ( target, interfaceType, timeoutDuration, timeoutUnit );
	}

	@Override
	public <T> T newProxy ( T target, Class<T> interfaceType, Duration timeout )
	{
		return delegate.newProxy ( target, interfaceType, timeout );
	}

	@Override
	public <T> T callWithTimeout ( Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit )
	{
		try {
			return delegate.callWithTimeout ( callable, timeoutDuration, timeoutUnit );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
		catch ( InterruptedException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Interruption during timed execution", ex	
			);
		}
		catch ( ExecutionException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Error during timed execution: $cause", ex	
			);
		}
	}

	@Override
	public <T> T callWithTimeout ( Callable<T> callable, Duration timeout )
	{
		try {
			return delegate.callWithTimeout ( callable, timeout );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
		catch ( InterruptedException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Interruption during timed execution", ex	
			);
		}
		catch ( ExecutionException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Error during timed execution: $cause", ex	
			);
		}
	}

	@Override
	public <T> T callUninterruptiblyWithTimeout ( Callable<T> callable, long timeoutDuration, TimeUnit timeoutUnit )
	{
		try {
			return delegate.callUninterruptiblyWithTimeout ( callable, timeoutDuration, timeoutUnit );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
		catch ( ExecutionException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Error during timed execution: $cause", ex	
			);
			} 		
	}

	@Override
	public <T> T callUninterruptiblyWithTimeout ( Callable<T> callable, Duration timeout )
	{
		try {
			return delegate.callUninterruptiblyWithTimeout ( callable, timeout );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
		catch ( ExecutionException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Error during timed execution: $cause", ex	
			);
		} 		
	}

	@Override
	public void runWithTimeout ( Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit )
	{
		try {
			delegate.runWithTimeout ( runnable, timeoutDuration, timeoutUnit );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
		catch ( InterruptedException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Interruption during timed execution", ex	
			);
		}
	}

	@Override
	public void runWithTimeout ( Runnable runnable, Duration timeout )
	{
		try {
			delegate.runWithTimeout ( runnable, timeout );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
		catch ( InterruptedException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedInterruptedException.class, ex, 
				"Interruption during timed execution", ex	
			);
		}
	}

	@Override
	public void runUninterruptiblyWithTimeout ( Runnable runnable, long timeoutDuration, TimeUnit timeoutUnit )
	{
		try {
			delegate.runUninterruptiblyWithTimeout ( runnable, timeoutDuration, timeoutUnit );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
	}
	
	@Override
	public void runUninterruptiblyWithTimeout ( Runnable runnable, Duration timeout )
	{
		try {
			delegate.runUninterruptiblyWithTimeout ( runnable, timeout );
		}
		catch ( TimeoutException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedTimeoutException.class, ex, 
				"Timeout during timed execution", ex	
			);
		}
	}
}
