package uk.ac.ebi.utils.streams;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
	
}
