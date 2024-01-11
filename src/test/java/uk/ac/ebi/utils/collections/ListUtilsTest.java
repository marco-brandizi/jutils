package uk.ac.ebi.utils.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>5 Apr 2023</dd></dl>
 *
 */
public class ListUtilsTest
{

	@Test
	public void testGetRow ()
	{
		String[] headers = new String [] { "name", "surname", "age", "height (cm)" };
		Object[] row = new Object[] { "Mr", "Bean", 42, 170.2 };
		
		OptionsMap rowMap = ListUtils.getRow ( headers, row );

		assertNotNull ( "Result is null!", rowMap );
		assertEquals ( "Wrong result size!", headers.length, rowMap.size () );
		
		verifyGetRow ( headers, row, rowMap );
	}

	@Test
	public void testGetRowNullRow ()
	{
		String[] headers = new String [] { "name", "surname", "age", "height (cm)" };
		Object[] row = new Object[] {};
		
		OptionsMap rowMap = ListUtils.getRow ( headers, row );

		assertNotNull ( "Result is null!", rowMap );
		assertEquals ( "Wrong result size!", 0, rowMap.size () );
	}

	@Test
	public void testGetRowPartialRow ()
	{
		String[] headers = new String [] { "name", "surname", "age", "height (cm)" };
		Object[] row = new Object[] { null, "Bean", 42 };
		
		OptionsMap rowMap = ListUtils.getRow ( headers, row );

		assertNotNull ( "Result is null!", rowMap );
		assertEquals ( "Wrong result size!", 2, rowMap.size () );
		verifyGetRow ( headers, row, rowMap );
	}
	
	
	private void verifyGetRow ( String[] headers, Object[] row, OptionsMap rowMap )
	{
		int ncols = Math.min ( headers.length, row.length );
		for ( int j = 0; j < ncols; j++ )
		{
			String h = headers [ j ];
			
			assertEquals (  
				String.format ( "Wrong result value for '%s'!", h ),
				row [ j ],
				rowMap.get ( h )
			);
		}
	}
}
