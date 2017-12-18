package uk.ac.ebi.utils.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.utils.exceptions.UnexpectedEventException;


/**
 * A simple class to manage processing of data in multi-thread mode.
 * 
 * The idea implemented here is that {@link #process(Object)} runs a loop where: 
 * 
 * <ul>
 *   <li>gets a data item from its input source of type S</li>
 *   <li>optionally transforms the data item and sends the result to the current destination of type D</li>
 *   <li>invokes {@link #handleNewTask(Object, boolean)}, which 
 *   {@link #decideNewTask(Object) decides} if it's time to issue a new {@link #getConsumer() processing thread}.
 *   The latter possibly create a new destination object, via {@link #getDestinationSupplier()} and hence
 *   the caller should assign the current destination to it.</li>
 * </ul> 
 *
 * Each new invocation performed by {@link #handleNewTask(Object, boolean)} consists of the creation of a new
 * task which is submitted an {@link #getExecutor() executor service} and hence the processing can happen in 
 * multi-thread mode.
 * 
 * You can find a usage example of this class in the <a href = "TODO">rdf-utils-jena package</a>.
 *  
 * @author brandizi
 * <dl><dt>Date:</dt><dd>1 Dec 2017</dd></dl>
 *
 */
public abstract class BatchProcessor<S, D>
{
	private Consumer<D> consumer;
	private Supplier<D> destinationSupplier;
	
	private ExecutorService executor;
	
	protected Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	public BatchProcessor ()
	{
		int poolSize = Runtime.getRuntime().availableProcessors();
		
		this.executor = new ThreadPoolExecutor (
			poolSize, 
			Integer.MAX_VALUE, 
			0L, TimeUnit.MILLISECONDS, 
			new LinkedBlockingQueue<> ( poolSize * 2 ) 
		);
	}
	
	/**
	 * <b>WARNING</b>: in addition to the behaviour explained above, this method should also invoke 
	 * {@link #waitExecutor(String)}.
	 */
	public abstract void process ( S source, Object... opts );

	public void process ( S source ) {
		this.process ( source, new Object [ 0 ] );
	}

	
	protected D handleNewTask ( D currentDest ) {
		return handleNewTask ( currentDest, false );
	}

	/**
	 * This is the method that possibly issues a new task, via the {@link #getExecutor()}, which runs 
	 * the {@link #consumer} against the current {@link #getDestinationSupplier() destination}. 
	 * 
	 * @param forceFlush if true it flushes the data independently of {@link #getChunkSize()}.	 
	 */
	protected D handleNewTask ( D currentDest, boolean forceFlush )
	{
		if ( !( forceFlush || this.decideNewTask ( currentDest ) ) ) return currentDest;

		executor.submit ( new Runnable() 
		{
			@Override
			public void run () {
				try {
					consumer.accept ( currentDest );
				}
				catch ( Exception ex ) {
					log.error ( 
						String.format ( 
							"Error while running batch processor thread %s: %s", 
							Thread.currentThread ().getName (), ex.getMessage () 
						),
						ex
					);
				}
			}
		});
		
		return this.destinationSupplier.get ();
	}
	
	/**
	 * {@link #handleNewTask(Object, boolean)} decides to generate a new task based on the value returned
	 * by this.
	 * 
	 */
	protected abstract boolean decideNewTask ( D dest );

	/**
	 * Every new parallel task that is generated by {@link #process(Object)} and {@link #handleNewTask(Object, boolean)}
	 * runs this consumer.
	 */
	public Consumer<D> getConsumer ()
	{
		return consumer;
	}

	public BatchProcessor<S, D> setConsumer ( Consumer<D> consumer )
	{
		this.consumer = consumer;
		return this;
	}

	/**
	 * Every time that {@link #handleNewTask(Object, boolean)} decides it's time to work on a new 
	 * {@link #getConsumer() task} and destination D, this supplier is used to generate such new destination.
	 * 
	 */
	public Supplier<D> getDestinationSupplier ()
	{
		return destinationSupplier;
	}

	public BatchProcessor<S, D> setDestinationSupplier ( Supplier<D> destinationSupplier )
	{
		this.destinationSupplier = destinationSupplier;
		return this;
	}


	/**
	 * The thread pool manager used by {@link #export(ONDEXGraph)}. By default this is 
	 * {@link ThreadPoolExecutor 
	 * ThreadPoolExecutor( &lt;available processors&gt;, Integer.MAX_VALUE, ..., LinkedBlockingQueue (processors*2) )},
	 * that is, a pool where a fixed number of threads is running at any time (up to the 
	 * {@link Runtime#availableProcessors() number of processors available}) and where the  
	 * {@link ExecutorService#submit(Runnable) task submission operation} is also put on hold if the 
	 * pool is full, waiting for some thread to finish its job.
	 * 
	 * Normally you shouldn't need to change this parameter, except, maybe, where parallelism isn't such 
	 * worth and hence you prefer a fixed pool of size 1 ({@link RDFFileExporter} does so).
	 * 
	 */
	public ExecutorService getExecutor ()
	{
		return executor;
	}

	public BatchProcessor<S, D> setExecutor ( ExecutorService executor )
	{
		this.executor = executor;
		return this;
	}
			
	/**
	 * Waits that all the parallel jobs submitted to the processor are finished. It keeps polling
	 * {@link ExecutorService#isTerminated()} and invoking {@link ExecutorService#awaitTermination(long, TimeUnit)}.
	 * 
	 * @param pleaseWaitMessage the message to be reported (via logger/INFO level) while waiting.
	 */
	protected void waitExecutor ( String pleaseWaitMessage )
	{
		executor.shutdown ();

		// Wait to finish
		try
		{
			while ( !executor.isTerminated () ) 
			{
				log.info ( pleaseWaitMessage ); 
				executor.awaitTermination ( 5, TimeUnit.MINUTES );
			}
		}
		catch ( InterruptedException ex ) {
			throw new UnexpectedEventException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
}