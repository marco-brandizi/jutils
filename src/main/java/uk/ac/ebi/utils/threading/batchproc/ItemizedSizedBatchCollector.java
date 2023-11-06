package uk.ac.ebi.utils.threading.batchproc;

/**
 * A common type of batch collector which manages a flow of items as input and which considers the batch full when 
 * it has {@link #accumulator() accumulated} a {@link #maxBatchSize() given number of items}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 * @deprecated the functionality available in this package is provided by project 
 * Reactor and we recommend to switch to that. TODO: helpers and examples about batching via Reactor.
 */
@Deprecated
public interface ItemizedSizedBatchCollector<B,E> 
	extends ItemizedBatchCollector<B, E>, SizedBatchCollector<B>
{
	// Doesn't need anything, it's just a signature
}
