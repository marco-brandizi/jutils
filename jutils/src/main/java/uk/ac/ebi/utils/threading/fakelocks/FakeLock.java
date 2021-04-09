package uk.ac.ebi.utils.threading.fakelocks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import uk.ac.ebi.utils.runcontrol.ProgressLogger;

/**
 * A fake lock, which doesn't actually locking anything.
 * 
 * This can be a quick replacement for code that you're sure you'll be use in a single thread.
 * By doing such a replacement, you'll improve in performance.
 * 
 * @see ProgressLogger
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Apr 2021</dd></dl>
 *
 */
public class FakeLock implements Lock
{

	public FakeLock ()
	{
	}

	@Override
	public void lock ()
	{
	}

	@Override
	public void lockInterruptibly () throws InterruptedException
	{
	}

	@Override
	public boolean tryLock ()
	{
		return true;
	}

	@Override
	public boolean tryLock ( long time, TimeUnit unit ) throws InterruptedException
	{
		return true;
	}

	@Override
	public void unlock ()
	{
	}

	@Override
	public Condition newCondition ()
	{
		return new FakeCondition ();
	}

}
