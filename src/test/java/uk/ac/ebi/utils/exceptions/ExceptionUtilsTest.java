package uk.ac.ebi.utils.exceptions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.getSignificantException;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.getSignificantMessage;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Aug 2018</dd></dl>
 *
 */
public class ExceptionUtilsTest
{
	@Test
	public void testBuildEx ()
	{
		RuntimeException ex = ExceptionUtils.buildEx ( 
			RuntimeException.class, "A test exception with param having value: %.1f param", 10.5 
		);
		
		Assert.assertTrue ( "Unexpected message!", ex.getMessage ().contains ( "10.5 param" ) );
	}

	@Test
	public void testBuildExCause ()
	{
		RuntimeException ex = ExceptionUtils.buildEx ( 
			RuntimeException.class, new IOException (), 
			"A test exception with param having value: %.1f param", 
			10.5 
		);
		
		Assert.assertTrue ( "Unexpected message!", ex.getMessage ().contains ( "10.5 param" ) );
		Assert.assertTrue ( "Unexpected cause!", ex.getCause () instanceof IOException );
	}

	@Test
	public void testBuildExCauseUncheckedEx ()
	{
		var originalEx = new IOException ( "Original I/O error" );
		UncheckedIOException ex = ExceptionUtils.buildEx ( 
			UncheckedIOException.class, originalEx, 
			"A wrapped I/O exception: %s",
			originalEx.getMessage ()
		);
		
		Assert.assertTrue ( "Unexpected message (wrapper)!", ex.getMessage ().contains ( "A wrapped I/O exception" ) );
		Assert.assertTrue ( "Unexpected message (original)!", ex.getMessage ().contains ( originalEx.getMessage () ) );
		Assert.assertEquals ( "Unexpected cause!", originalEx, ex.getCause () );
	}
	
	
	
	@Test ( expected = Exception.class )
	public void testThrowEx () throws Exception
	{
		ExceptionUtils.throwEx ( Exception.class, "Test Exception" );
	}

	@Test ( expected = RuntimeException.class )
	public void testThrowExUnchecked ()
	{
		ExceptionUtils.throwEx ( 
			RuntimeException.class, new IOException (), "Test Exception with %s param", "foo" 
		);
	}
	
	@Test
	public void testBuildExCauseWithoutConstructor ()
	{
		NumberFormatException ex = new NumberFormatException ( "Exception that doesn't accept a cause in its constructor" );
		String msgTpl = "Parent Exception. Child message is: %s";
		NumberFormatException parentEx = ExceptionUtils.buildEx (
			ex, msgTpl, ex.getMessage ()
		);
		
		assertNotNull ( "Cause is null!", parentEx.getCause () );
		assertEquals ( "Wrong parent's messsage!", String.format ( msgTpl, ex.getMessage () ), parentEx.getMessage () );
	}
	
	@Test
	public void testGetSignificantException ()
	{
		var msg = "I've a message!";
		var ex1 = new Exception ( "I've a message!" );
		var ex2 = new Exception ( null, ex1 );
		var ex3 = new Exception ( null, ex2 );
		
		assertEquals ( "Wrong ex fetched by testGetSignificantException()", ex1, getSignificantException ( ex3 ) );
		assertEquals ( "Wrong message fetched by testGetSignificantException()", msg, getSignificantMessage ( ex3 ) );
		
		var exNull = new Exception ( null, new Exception ( (String) null ) );
		assertNull ( "getSignificantException() for null didn't work!", getSignificantException ( exNull ) );
	}
	
	@Test
	public void testCauseMessageInjection ()
	{
		var cause = new Exception ( "some weird error" );
	
		var ex = ExceptionUtils.buildEx ( RuntimeException.class, cause, "Something happened down here: $cause." );
		assertTrue ( "$cause not replaced!", ex.getMessage ().contains ( "here: " + cause.getMessage () + "." ) );

		ex = ExceptionUtils.buildEx ( RuntimeException.class, cause, "Something happened down here: ${cause}" );
		assertTrue ( "${cause} not replaced!", ex.getMessage ().endsWith ( "here: " + cause.getMessage () ) );
	}
}
