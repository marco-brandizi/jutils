package uk.ac.ebi.utils.collections;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>16 Apr 2024</dd></dl>
 *
 */
public class CollectionsUtilsTest
{
	@Test
	public void testAsSet ()
	{
		String s = "Hello, World";
		Set<String> set = CollectionsUtils.asSet ( s );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", 1, set.size () );
		assertTrue ( "Bad result value!", set.contains ( s ) );		
	}

	@Test
	public void testAsSetWithSet ()
	{
		Set<Integer> testSet = Set.of ( 1, 2, 3 );
		Set<String> set = CollectionsUtils.asSet ( testSet );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", testSet.size (), set.size () );
		assertEquals ( "Bad result value!", testSet, set );		
	}

	@Test
	public void testAsSetWithNull ()
	{
		Set<String> set = CollectionsUtils.asSet ( null );
		
		assertNotNull ( "null result!", set );
		assertTrue ( "Bad result size!", set.isEmpty () );
	}	

	@Test
	public void testAsSetWithCollection ()
	{
		List<Integer> testList = List.of ( 1, 2, 3 );
		Set<Integer> set = CollectionsUtils.asSet ( testList );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", testList.size (), set.size () );
		
		testList.stream ()
		.forEach ( e -> assertTrue ( 
			format ( "Bad result content (doesn't contain %s)!", e),
			set.contains ( e ) 
		));
	}
	
	
	@Test
	public void testAsList ()
	{
		String s = "Hello, World";
		List<String> list = CollectionsUtils.asList ( s );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", 1, list.size () );
		assertTrue ( "Bad result value!", list.contains ( s ) );		
	}

	@Test
	public void testAsListWithList ()
	{
		List<Integer> testList = List.of ( 1, 2, 3 );
		List<String> list = CollectionsUtils.asList ( testList );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", testList.size (), list.size () );
		assertEquals ( "Bad result value!", testList, list );		
	}

	@Test
	public void testAsListWithNull ()
	{
		List<String> list = CollectionsUtils.asList ( null );
		
		assertNotNull ( "null result!", list );
		assertTrue ( "Bad result size!", list.isEmpty () );
	}
	
	@Test
	public void testAsListWithCollection ()
	{
		Set<Integer> testSet = Set.of ( 1, 2, 3 );
		List<Integer> list = CollectionsUtils.asList ( testSet );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", testSet.size (), list.size () );
		
		testSet.stream ()
		.forEach ( e -> assertTrue ( 
			format ( "Bad result content (doesn't contain %s)!", e),
			list.contains ( e ) 
		));
	}	
}
