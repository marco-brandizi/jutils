package uk.ac.ebi.utils.threading.batchproc;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import uk.ac.ebi.utils.opt.runcontrol.ReactorUtils;

/**
 * <h1>The Batch Collector</h1>
 * 
 * A batch collector is a container of methods to manage batches during the operations of 
 * {@link BatchProcessor}. This default interface offers a {@link #batchFactory() batch factory}, used
 * to obtain a new batch when needed, and a {@link #batchReadyFlag() ready flag}, which establishes if
 * a batch is ready for being processed (eg, it's full wrt a given size).  
 * 
 * Significant specific sub-interfaces are the {@link SizedBatchCollector sized-based collectors}, the
 * {@link ItemizedSizedBatchCollector item-based one} and their {@link ItemizedSizedBatchCollector intersection}.  
 *
 * Due to some similarity, batch collectors are named after {@link Collector Java stream collectors}.
 *
 * @param <B> the type of batch that the collector manages.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>23 Nov 2019</dd></dl>
 *
 * @deprecated the functionality available in this package is provided by project 
 * Reactor and we recommend to switch to that. @see ReactorUtils
 */
@Deprecated
public interface BatchCollector<B>
{
	public abstract Supplier<B> batchFactory();
	public abstract Predicate<B> batchReadyFlag();
}
