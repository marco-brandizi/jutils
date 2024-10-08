package uk.ac.ebi.utils.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>7 Oct 2024</dd></dl>
 *
 */
public class IteratorUtils
{
	private IteratorUtils ()
	{
		// It's a function collection
	}

	/**
	 * Builds an {@link Iterator} of non-null based on a supplier.
	 *  
	 * @param supplier provides an element every time the resulting interator 
	 * requests for it. The first time this offers a null, the iterator starts 
	 * returning false in {@link Iterator#hasNext()} and the iteration is over.
	 * 
	 */
	public static <E> Iterator<E> supplier2Iterator ( Supplier<? extends E> supplier )
	{
		return new Iterator<>()
		{
			private E nextElement;
			private boolean isFinished = false;
			
			@Override
			public boolean hasNext ()
			{
				if ( isFinished ) return false;
				if ( nextElement != null ) return true;
				
				nextElement = supplier.get ();
				if ( nextElement != null ) return true;
				
				isFinished = true;
				return false;
			}

			@Override
			public E next ()
			{
				if ( !hasNext () ) throw new NoSuchElementException (
					"Supplier-based iterator, no more elements available"	
				);
				E result = nextElement;
				nextElement = null;
				return result;
			}
		}; // return new Iterator()
	} // supplier2Iterator
	
}
