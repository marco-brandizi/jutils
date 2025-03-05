package uk.ac.ebi.utils.collections;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
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
	public void testAsUnmodifiableSet ()
	{
		String s = "Hello, World";
		Set<String> set = CollectionsUtils.asUnmodifiableSet ( s );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", 1, set.size () );
		assertTrue ( "Bad result value!", set.contains ( s ) );
		
		try {
			set.add ( "Another" );
			Assert.fail ( "Set looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}
	}

	@Test
	public void testAsUnmodifiableSetWithSet ()
	{
		Set<Integer> testSet = Set.of ( 1, 2, 3 );
		Set<Integer> set = CollectionsUtils.asUnmodifiableSet ( testSet );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", testSet.size (), set.size () );
		assertEquals ( "Bad result value!", testSet, set );
		
		try {
			set.add ( 100 );
			Assert.fail ( "Set looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}		
	}

	@Test
	public void testAsUnmodifiableSetWithNull ()
	{
		Set<String> set = CollectionsUtils.asUnmodifiableSet ( null );
		
		assertNotNull ( "null result!", set );
		assertTrue ( "Bad result size!", set.isEmpty () );
		
		try {
			set.add ( "Another" );
			Assert.fail ( "Set looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}		
	}	

	@Test
	public void testAsUnmodifiableSetWithCollection ()
	{
		List<Integer> testList = List.of ( 1, 2, 3 );
		Set<Integer> set = CollectionsUtils.asUnmodifiableSet ( testList );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", testList.size (), set.size () );
		
		testList.stream ()
		.forEach ( e -> assertTrue ( 
			format ( "Bad result content (doesn't contain %s)!", e),
			set.contains ( e ) 
		));
		
		try {
			set.add ( 100 );
			Assert.fail ( "Set looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}
		
	}
	
	
	@Test
	public void testAsUnmodifiableList ()
	{
		String s = "Hello, World";
		List<String> list = CollectionsUtils.asUnmodifiableList ( s );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", 1, list.size () );
		assertTrue ( "Bad result value!", list.contains ( s ) );
		
		try {
			list.add ( "Another" );
			Assert.fail ( "List looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}		
	}

	@Test
	public void testAsUnmodifiableListWithList ()
	{
		List<Integer> testList = List.of ( 1, 2, 3 );
		List<Integer> list = CollectionsUtils.asUnmodifiableList ( testList );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", testList.size (), list.size () );
		assertEquals ( "Bad result value!", testList, list );
		
		try {
			list.add ( 100 );
			Assert.fail ( "List looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}				
	}

	@Test
	public void testAsUnmodifiableListWithNull ()
	{
		List<String> list = CollectionsUtils.asUnmodifiableList ( null );
		
		assertNotNull ( "null result!", list );
		assertTrue ( "Bad result size!", list.isEmpty () );
		
		try {
			list.add ( "First" );
			Assert.fail ( "List looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}		
	}
	
	@Test
	public void testAsUnmodifiableListWithCollection ()
	{
		Set<Integer> testSet = Set.of ( 1, 2, 3 );
		List<Integer> list = CollectionsUtils.asUnmodifiableList ( testSet );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", testSet.size (), list.size () );
		
		testSet.stream ()
		.forEach ( e -> assertTrue ( 
			format ( "Bad result content (doesn't contain %s)!", e),
			list.contains ( e ) 
		));
		
		try {
			list.add ( 100 );
			Assert.fail ( "List looks modifiable!" );
		}
		catch ( UnsupportedOperationException ex ) {
			// Nothing to do
		}		
	}
	
	
	
	@Test
	public void testAsSet ()
	{
		String s = "Hello, World";
		Set<String> set = CollectionsUtils.asSet ( s );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", 1, set.size () );
		assertTrue ( "Bad result value!", set.contains ( s ) );
		
		set.add ( "Another" );
		assertEquals ( "Returned set didn't change!", 2, set.size () );
	}

	@Test
	public void testAsSetWithSet ()
	{
		Set<Integer> testSet = Set.of ( 1, 2, 3 );
		testSet = new HashSet<> ( testSet );
		Set<Integer> set = CollectionsUtils.asSet ( testSet );
		
		assertNotNull ( "null result!", set );
		assertEquals ( "Bad result size!", testSet.size (), set.size () );
		assertEquals ( "Bad result value!", testSet, set );
		
		set.add ( 100 );
		assertEquals ( "Returned set didn't change!", 4, set.size () );
		assertEquals ( "The returned list isn't the original one!", 4, testSet.size () );
	}

	@Test
	public void testAsSetWithNull ()
	{
		Set<String> set = CollectionsUtils.asSet ( null );
		
		assertNotNull ( "null result!", set );
		assertTrue ( "Bad result size!", set.isEmpty () );
		
		set.add ( "First" );
		assertFalse ( "Returned set didn't change!", set.isEmpty () );
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
		
		set.add ( 100 );
		assertEquals ( "Returned set didn't change!", 4, set.size () );
		
	}
	
	
	@Test
	public void testAsList ()
	{
		String s = "Hello, World";
		List<String> list = CollectionsUtils.asList ( s );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", 1, list.size () );
		assertTrue ( "Bad result value!", list.contains ( s ) );
		
		list.remove ( 0 );
		assertTrue ( "List didn't change!", list.isEmpty () );
	}

	@Test
	public void testAsListWithList ()
	{
		List<Integer> testList = new LinkedList<> ( List.of ( 1, 2, 3 ) );
		List<Integer> list = CollectionsUtils.asList ( testList );
		
		assertNotNull ( "null result!", list );
		assertEquals ( "Bad result size!", testList.size (), list.size () );
		assertEquals ( "Bad result value!", testList, list );
				
		list.add ( 100 );
		assertEquals ( "List didn't change!", 4, list.size () );
		assertEquals ( "The returned list isn't the original one!", 4, testList.size () );
	}

	@Test
	public void testAsListWithNull ()
	{
		List<String> list = CollectionsUtils.asList ( null );
		
		assertNotNull ( "null result!", list );
		assertTrue ( "Bad result size!", list.isEmpty () );
		
		list.add ( "First" );
		assertFalse ( "List didn't change!", list.isEmpty () );
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
		
		list.remove ( 0 );
		assertEquals ( "List didn't change!", 2, list.size () );
	}
}
