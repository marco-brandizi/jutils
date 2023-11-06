package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.BiConsumer;

/**
 * A batch collector that can be populated one item at a time. This means that this collector has 
 * an additional method to {@link #accumulator() add elements to the batch}.
 *
 * @param <E> the type of elements that the batch can store.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 * @deprecated the functionality available in this package is provided by project 
 * Reactor and we recommend to switch to that. TODO: helpers and examples about batching via Reactor.
 */
@Deprecated
public interface ItemizedBatchCollector<B,E> extends BatchCollector<B>
{
	public BiConsumer<B, E> accumulator();
}
