package uk.ac.ebi.utils.threading.batchproc2;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 Oct 2023</dd></dl>
 *
 */
public interface BatchCollector<B>
{
	B newBatch ();
	boolean isReady ( B batch );
}
