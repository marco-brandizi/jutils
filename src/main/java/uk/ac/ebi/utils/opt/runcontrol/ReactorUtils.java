package uk.ac.ebi.utils.opt.runcontrol;

import static reactor.core.scheduler.Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE;
import static reactor.core.scheduler.Schedulers.newBoundedElastic;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Utilities based on the Project Reactor library.
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>29 Jun 2024</dd></dl>
 *
 */
public class ReactorUtils
{
	/**
	 * {@link Schedulers#newBoundedElastic(int, int, String)} with the {@link Schedulers#DEFAULT_BOUNDED_ELASTIC_SIZE default threadCap} 
	 * and a low limit for queuedTaskCap. This is suitable for cases where the source is
	 * much faster than the downstream processing and hence there is little point with queueing 
	 * too much stuff.
	 * 
	 */
	public static final Scheduler DEFAULT_FLUX_SCHEDULER = newBoundedElastic ( 
		DEFAULT_BOUNDED_ELASTIC_SIZE, 100, 
		"jutils.batchSched" 
	);
	
	/**
	 * This has been tested in tasks like saving data on a database.
	 */
	public static final int DEFAULT_BATCH_SIZE = 2500;

	/**
	 * Little helper to build a common {@link ParallelFlux} to process a source of items
	 * in parallel batches.
	 *
	 * @author Marco Brandizi
	 * <dl><dt>Date:</dt><dd>30 Jun 2024</dd></dl>
	 *
	 */
	public static class ParallelBatchFluxBuilder<T, B extends Collection<T>>
	{
		private Flux<T> flux;
		private Scheduler scheduler = DEFAULT_FLUX_SCHEDULER;
		private int batchSize = DEFAULT_BATCH_SIZE;
		private Supplier<B> batchSupplier;
		
		@SuppressWarnings ( "unchecked" )
		public ParallelBatchFluxBuilder ( Flux<? extends T> flux )
		{
			this.flux = (Flux<T>) flux;
		}
		
		public ParallelBatchFluxBuilder ( Stream<? extends T> stream )
		{
			this ( Flux.fromStream ( stream ) );
		}

		public ParallelBatchFluxBuilder ( Collection<? extends T> collection )
		{
			this ( collection.stream () );
		}
		
		public ParallelFlux<B> build ()
		{
			@SuppressWarnings ( "unchecked" )
			Flux<B> result = this.batchSupplier == null 
				? (Flux<B>) flux.buffer ( batchSize ) : flux.buffer ( batchSize, batchSupplier );
			
			return result
			.parallel ()
			.runOn ( scheduler );
		}
		
		/**
		 * Default is {@link ReactorUtils#DEFAULT_FLUX_SCHEDULER}.
		 */
		public ParallelBatchFluxBuilder<T, B> withScheduler ( Scheduler scheduler )
		{
			this.scheduler = scheduler;
			return this;
		}
		
		/**
		 * Default it {@link ReactorUtils#DEFAULT_BATCH_SIZE}.
		 */
		public ParallelBatchFluxBuilder<T, B> withBatchSize ( int batchSize )
		{
			this.batchSize = batchSize;
			return this;
		}

		/**
		 * Default is null, which fallback to {@link Flux#buffer(int)}, usually a {@link List} supplier.
		 */
		@SuppressWarnings ( "unchecked" )
		public ParallelBatchFluxBuilder<T, B> withBatchSupplier ( Supplier<? extends Collection<? super T>> batchSupplier )
		{
			this.batchSupplier = (Supplier<B>) batchSupplier;
			return this;
		}
	} // class ParallelBatchFluxBuilder
	
	
	/**
	 * Uses {@link ParallelBatchFluxBuilder} to process a source of batches.
	 */
	public static <T, B extends Collection<? super T>> void batchProcessing (
	  ParallelFlux<B> parallelFlux, Consumer<B> task		
	)
	{
		parallelFlux.doOnNext ( task )
		.sequential ()
		.blockLast ();
	}

	/**
	 * Uses {@link ParallelBatchFluxBuilder} with default options and
	 * {@link #batchProcessing(ParallelFlux, Consumer)} to batch a source of items and 
	 * process them in parallel batches.
	 *   
	 */
	public static <T> void batchProcessing (
		Flux<T> flux, Consumer<List<T>> task		
	)
	{
		ParallelFlux<List<T>> parFlux = new ParallelBatchFluxBuilder<T,List<T>> ( flux ).build ();
		batchProcessing ( parFlux, task );
	}

	/**
	 * Variant of {@link #batchProcessing(Flux, Consumer)}
	 */
	public static <T> void batchProcessing (
		Stream<T> stream, Consumer<List<T>> task		
	)
	{
		ParallelFlux<List<T>> parFlux = new ParallelBatchFluxBuilder<T, List<T>> ( stream ).build ();
		batchProcessing ( parFlux, task );
	}

	/**
	 * Variant of {@link #batchProcessing(Flux, Consumer)}
	 */
	public static <T> void batchProcessing (
		Collection<T> collection, Consumer<List<T>> task		
	)
	{
		ParallelFlux<List<T>> parFlux = new ParallelBatchFluxBuilder<T,List<T>> ( collection ).build ();
		batchProcessing ( parFlux, task );
	}
	
}
