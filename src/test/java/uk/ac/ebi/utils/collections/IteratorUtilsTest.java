package uk.ac.ebi.utils.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;

/**
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>7 Oct 2024</dd></dl>
 *
 */
public class IteratorUtilsTest
{
	@Test
	public void testSupplier2Iterator ()
	{
		Integer n = 10;
		MutableInt i = new MutableInt ();
		
		Iterator<Integer> itr = IteratorUtils.supplier2Iterator ( () -> {
			int i1 = i.getAndIncrement (); 
			return i1 < n ? i1 : null;
		});
		
		Integer i1 = 0;
		while ( itr.hasNext () )
			assertEquals ( "Wrong value returned by the iterator!", i1++, itr.next () );
		
		assertEquals ( "Wrong end value after the iteration", n, i1 );
	}
	
	@Test
	public void testSupplier2IteratorEmptySupplier ()
	{
		Iterator<Integer> itr = IteratorUtils.supplier2Iterator ( () -> null );
		assertFalse ( "Empty iterator has an element!", itr.hasNext () );
	}
}
