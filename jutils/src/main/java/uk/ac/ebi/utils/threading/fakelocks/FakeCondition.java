package uk.ac.ebi.utils.threading.fakelocks;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * @see FakeLock
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Apr 2021</dd></dl>
 *
 */
public class FakeCondition implements Condition
{

	public FakeCondition ()
	{
	}

	@Override
	public void await () throws InterruptedException
	{
	}

	@Override
	public void awaitUninterruptibly ()
	{
	}

	@Override
	public long awaitNanos ( long nanosTimeout ) throws InterruptedException
	{
		return nanosTimeout;
	}

	@Override
	public boolean await ( long time, TimeUnit unit ) throws InterruptedException
	{
		return true;
	}

	@Override
	public boolean awaitUntil ( Date deadline ) throws InterruptedException
	{
		return true;
	}

	@Override
	public void signal ()
	{
	}

	@Override
	public void signalAll ()
	{
	}

}
