package uk.ac.ebi.utils.streams;

import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.util.Spliterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.Validate;

import uk.ac.ebi.utils.collections.TupleSpliterator;

/**
 * Stream Utils
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2017</dd></dl>
 *
 */
public class StreamUtils
{
	/**
	 * Returns a stream of tuples built from base streams.
	 * 
	 * This is done by populating each tuple item with one item from the underlining
	 * streams. This is basically a wrapper of {@link TupleSpliterator}, which is passed to
	 * {@link StreamSupport#stream(Spliterator, boolean)}. 
	 * 
	 * The built stream uses {@link TupleSpliterator#characteristics()}, which, in turn, are established on the
	 * basis of the underlining spliterators (see the method's javadoc).
	 * 
	 * @param isParallel this is passed to {@link StreamSupport#stream(Spliterator, boolean)}, since the underlining 
	 * iterator is immutable, creating a parallel stream as result shouldn't be a problem, unless you've some strange
	 * things in the base streams.
	 *  
	 * @param streams the base streams from which the result is built.
	 * 
	 * @see the unit tests for examples of usage.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Stream<T[]> tupleStream ( 
		boolean isParallel, Stream<? extends T>... streams 
	)
	{		
		if ( streams == null ) throw new NullPointerException ( 
			"Cannot create a tuple stream from a null stream array" 
		);
			
		Spliterator<Object> strmSpltrs[] = new Spliterator [ streams.length ];
		
		for ( int i = 0; i < streams.length; i++ )
			strmSpltrs [ i ] = (Spliterator<Object>) streams [ i ].spliterator ();
		
		TupleSpliterator<T> tupleItr = new TupleSpliterator<> ( (Spliterator<T>[]) strmSpltrs );
		return StreamSupport.stream ( tupleItr, isParallel );
	}
	
	/**
	 * Defaults to 0 (which implies {@link Spliterator#IMMUTABLE} only) and false (i.e., non-parallel result stream).
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Stream<T[]> tupleStream ( Stream<? extends T>... streams ) {
		return tupleStream ( false, streams );
	}
	
	/**
	 * <p>Returns a sampled stream, i.e., a stream where a quota of elements approximately equals to sampleRatio
	 * is returned.</p>
	 * 
	 * <p>This is obtained by attaching a filter to the initial stream that returns true if 
	 * {@code randomNumber[0,1) < sampleRatio}.</p>
	 * 
	 * sampleRatio must be between 0 and 1. 0 returns an empty stream, 1 returns the original stream.
	 *     
	 */
	public static <T> Stream<T> sampleStream ( Stream<T> stream, double samplingRatio )
	{
		Validate.notNull ( stream, "sampleStream() with null stream" );
		
		if ( samplingRatio < 0 || samplingRatio > 1 ) throwEx ( 
			IllegalArgumentException.class, 
			"sampleStream() with invalid sample ratio %f", samplingRatio 
		);
					
		return stream
			.filter ( e -> ThreadLocalRandom.current ().nextDouble ( 0, 1 ) < samplingRatio );
	}
	
	/**
	 *  A variant of {@link #sampleStream(Stream, double)} that computes the sampling ratio
	 *  as {@code sampleSize/totalSize}.
	 *  
	 *  @param totalSize must be >= 0. When 0, you'll end up having an empty stream.
	 *  
	 *  @param sampleSize must be < totalSize and >= 0. When 0, you'll end up having an empty 
	 *  stream. 
	 *  
	 */
	public static <T> Stream<T> sampleStream ( Stream<T> stream, long sampleSize, long totalSize )
	{
		if ( totalSize <= 0 ) throwEx ( 
			IllegalArgumentException.class, 
			"sampleStream() with invalid totalSize %d", totalSize 
		);			
					
		if ( sampleSize < 0 || sampleSize > totalSize ) throwEx ( 
			IllegalArgumentException.class, 
			"sampleStream() with invalid sampleSize %d", sampleSize 
		);			
		
		if ( totalSize == 0 ) return sampleStream ( stream, 0d );
		
		return sampleStream ( stream, 1d * sampleSize / totalSize );
	}
}
