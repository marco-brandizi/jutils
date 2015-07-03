package uk.ac.ebi.utils.time;

import org.apache.commons.lang3.time.StopWatch;

/**
 * A {@link StopWatch} with extended functionality.
 *
 * <dl><dt>date</dt><dd>9 Apr 2014</dd></dl>
 * @author Marco Brandizi
 *
 */
public class XStopWatch extends StopWatch
{
	/**
	 * If {@link #isSuspended()} invokes {@link #resume()} as usually. If {@link #isStopped()} calls {@link #start()}, 
	 * if neither is true, it means it's already started and hence does nothing.
	 */
	public void resumeOrStart ()
	{
		if ( isStopped () ) {
			super.reset ();
			super.start ();
		}
		else if ( isSuspended () ) super.resume ();
		// or, it is already running
	}
	
}
