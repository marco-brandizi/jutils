package uk.ac.ebi.utils.threading.batchproc.processors;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import uk.ac.ebi.utils.threading.batchproc.collectors.ListBatchCollector;

/**
 * @deprecated the functionality available in this package is provided by project 
 * Reactor and we recommend to switch to that. @see ReactorUtils
 */
@Deprecated
public abstract class ListBasedBatchProcessor<E, BJ extends Consumer<List<E>>>
	extends CollectionBasedBatchProcessor<E, List<E>, ListBatchCollector<E>, BJ>
{
	public ListBasedBatchProcessor ( BJ batchJob, Supplier<List<E>> batchFactory, int maxBatchSize ) {
		super ( batchJob, new ListBatchCollector<> ( batchFactory, maxBatchSize ) );
	}

	public ListBasedBatchProcessor ( BJ batchJob, int maxBatchSize ) 
	{
		this ( maxBatchSize );
		this.setBatchJob ( batchJob );
	}

	public ListBasedBatchProcessor ( int maxBatchSize )
	{
		this ();
		this.setMaxBatchSize ( maxBatchSize );
	}
	
	public ListBasedBatchProcessor () {
		super ( null, new ListBatchCollector<> () );
	}
}