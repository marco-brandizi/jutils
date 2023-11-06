package uk.ac.ebi.utils.threading.batchproc2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>31 Oct 2023</dd></dl>
 *
 */
public interface BatchSink<B>
{
	B getBatch ();
	void update ();
}
