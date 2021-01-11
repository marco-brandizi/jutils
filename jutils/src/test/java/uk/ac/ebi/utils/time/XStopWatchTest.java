package uk.ac.ebi.utils.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Assert;
import org.junit.Test;

import com.machinezoo.noexception.Exceptions;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>11 Jan 2021</dd></dl>
 *
 */
public class XStopWatchTest
{
	@Test
	public void testResumeOrStart () throws InterruptedException
	{
		XStopWatch chrono = new XStopWatch ();
		chrono.resumeOrStart ();
		assertTrue ( "Should be started!", chrono.isStarted () );
		
		Thread.sleep ( 1000 );
		long t1 = chrono.getTime ( TimeUnit.SECONDS );
		assertTrue ( "Unexpected measured time!", t1 >= 1 );

		chrono.suspend ();
		Thread.sleep ( 1000 );
		assertEquals ( "Unexpected measured time (1st check)!", t1, chrono.getTime ( TimeUnit.SECONDS ) );
		
		// Resumes from suspension
		chrono.resumeOrStart ();
		assertTrue ( "Should be resumed!", chrono.isStarted () );
		
		Thread.sleep ( 1000 );
		chrono.stop ();
		assertTrue ( "Unexpected measured time (after suspension+stop)!", chrono.getTime ( TimeUnit.SECONDS ) >= t1 + 1 );
		
		chrono.resumeOrStart ();
		assertTrue ( "Should be restarted!", chrono.isStarted () );
		Thread.sleep ( 1000 );
		assertTrue ( "Unexpected measured time (after 2nd restart)!", chrono.getTime ( TimeUnit.SECONDS ) >= 1 );
	}
	
	@Test
	public void testMultipleCalls () throws InterruptedException
	{
		XStopWatch chrono = new XStopWatch ();
		chrono.start ();
		assertTrue ( "Should be started!", chrono.isStarted () );

		Thread.sleep ( 1000 );
		assertTrue ( "Unexpected measured time!", chrono.getTime ( TimeUnit.SECONDS ) >= 1 );
		
		chrono.resumeOrStart ();
		assertTrue ( "Should be still started!", chrono.isStarted () );

		Thread.sleep ( 1000 );
		chrono.stop ();
		assertFalse ( "Should be stopped!", chrono.isStarted () );
		assertTrue ( "Unexpected measured time (2nd check)!", chrono.getTime ( TimeUnit.SECONDS ) >= 2 );

		// Restarts from 0
		chrono.restart ();
		assertTrue ( "Should be restarted!", chrono.isStarted () );
		Thread.sleep ( 1000 );
		assertTrue ( "Unexpected measured time (after restart)!", chrono.getTime ( TimeUnit.SECONDS ) >= 1 );
		
		// Restarts a non-stopped watch
		chrono.restart ();
		Thread.sleep ( 1000 );
		assertTrue ( "Unexpected measured time (after 2nd restart)!", chrono.getTime ( TimeUnit.SECONDS ) >= 1 );
	}

	
	@Test
	public void testProfiling () throws InterruptedException
	{
		// You can pass your own code as profile ( () -> <code> )
		// Here, we've it wrapped in the exception sneaking mechanism 
		long t1 = XStopWatch.profile ( Exceptions.sneak ().runnable ( () -> Thread.sleep ( 1000 ) ) );
		assertTrue ( "Unexpected measured time!", t1 >= 1000 );
	}
}
