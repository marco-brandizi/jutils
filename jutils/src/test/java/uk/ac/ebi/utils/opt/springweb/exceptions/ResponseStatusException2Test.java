package uk.ac.ebi.utils.opt.springweb.exceptions;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Jun 2021</dd></dl>
 *
 */
public class ResponseStatusException2Test
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	@Test
	public void testFull ()
	{
		var reason = "User not found";
		var ex = new ResponseStatusException2 ( 
			NOT_FOUND, reason, new RuntimeException ( "foo error" )
		);
		assertEquals ( "Wrong exception message!", reason + " (HTTP " + NOT_FOUND + ")", ex.getMessage () );
	}

	@Test
	public void testCodeMsg ()
	{
		var reason = "User not found";
		var ex = new ResponseStatusException2 ( NOT_FOUND.value (), reason, null );
		assertEquals ( "Wrong exception message!", reason + " (HTTP " + NOT_FOUND + ")", ex.getMessage () );
	}


	@Test
	public void testCodeNoMsg ()
	{
		var ex = new ResponseStatusException2 ( NOT_FOUND, null, null );
		assertEquals ( "Wrong exception message!", NOT_FOUND.getReasonPhrase () + " (HTTP " + NOT_FOUND + ")", ex.getMessage () );
	}

	@Test
	public void testUnknownCodeNoMsg ()
	{
		var code = 520;
		var ex = new ResponseStatusException2 ( code, null, null );
		assertEquals ( "Wrong exception message!", "HTTP " + code, ex.getMessage () );
	}
	
}
