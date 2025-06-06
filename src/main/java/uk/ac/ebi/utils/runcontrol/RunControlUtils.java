package uk.ac.ebi.utils.runcontrol;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

/**
 * Various utilities to control {@link Runnable runnable procedures} and alike. 
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>3 Jun 2025</dd></dl>
 *
 */
public class RunControlUtils
{
	private static final ExecutorService TIME_LIMITER_SERVICE = Executors.newCachedThreadPool ();
	
	private RunControlUtils () {
	}

	/** 
	 * @return a Guava {@link SimpleTimeLimiter}, initialised with a suitable and static common 
	 * {@link ExecutorService}.
	 * 
	 * <b>WARNING</b>: this means you're trading the fact you don't need
	 * to create an executor with sharing one for all time-bounded tasks in your JVM.
	 */
	public static TimeLimiter createTimeLimiter ()
	{
		return SimpleTimeLimiter.create ( TIME_LIMITER_SERVICE );
	}
}
