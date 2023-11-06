package uk.ac.ebi.utils.threading.batchproc2;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>30 Oct 2023</dd></dl>
 *
 */
public abstract class BatchProcessor<B>
{
	private BatchCollector<B> batchCollector;
	private Consumer<B> batchJob;
	
	public abstract void process ();
	
	public static <B1> BatchProcessor<B1> create ( Consumer<BatchSink<B1>> emitter )
	{
		BatchProcessor<B1> processor = new BatchProcessor<> () 
		{
			@Override
			public void process ()
			{
				BatchCollector<B1> batchCollector = this.getBatchCollector ();
				Consumer<B1> batchJob = this.getBatchJob ();
				
				MutableObject<B1> currentBatch = new MutableObject<> ( batchCollector.newBatch () );
				
				BatchSink<B1> sink = new BatchSink<> () 
				{
					@Override
					public B1 getBatch () {
						return currentBatch.getValue ();
					}

					@Override
					public void update () 
					{
						if ( !batchCollector.isReady ( currentBatch.getValue () ) ) return;
						
						// TODO: submit()
						batchJob.accept ( currentBatch.getValue () );
						currentBatch.setValue ( batchCollector.newBatch () );
					}
				}; // BatchSink
				
				emitter.accept ( sink );				
			}
		}; // processor
		
		return processor;
	}

	public BatchCollector<B> getBatchCollector ()
	{
		return batchCollector;
	}

	public Consumer<B> getBatchJob ()
	{
		return batchJob;
	}
}
