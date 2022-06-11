package uk.ac.ebi.utils.threading.fakelocks;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * @see FakeLock
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Apr 2021</dd></dl>
 *
 */
public class FakeReadWriteLock implements ReadWriteLock
{
	public FakeReadWriteLock ()
	{
	}

	@Override
	public Lock readLock ()
	{
		return new FakeLock ();
	}

	@Override
	public Lock writeLock ()
	{
		return new FakeLock ();
	}
}
