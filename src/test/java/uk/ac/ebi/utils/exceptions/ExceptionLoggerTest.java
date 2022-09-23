package uk.ac.ebi.utils.exceptions;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 *
 * @author joji c unnunni
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>22 Aug 2022</dd></dl>
 *
 */
public class ExceptionLoggerTest
{
	@Rule
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests ();

	/** This shows the typical way you're going to use it **/
	private final ExceptionLogger exlog = ExceptionLogger.getLogger ( getClass () );
	
	/** Not much need to change it all the time **/
	private final RuntimeException testEx = new RuntimeException ( "raised on-purpose" );

	
	@Test
	public void testBasics ()
	{
		var msg = "Testing the Runtime Exception Logger";
		exlog.logEx ( msg, testEx );

		var out = systemOutRule.getLog (); 
		
		assertTrue ( "Error message not found!", out.contains ( msg + ". Error: " + testEx.getMessage () ) );
		
		assertTrue (
			"Debug details not found!",
			out.contains ( msg + ". Details:" ) &&
			out.contains ( "java.lang.RuntimeException: " + testEx.getMessage () )
		);

	}
	
	@Test
	public void testParams ()
	{
		var msgPrefix = "Testing the Runtime Exception Logger, with params:";
		String p1 = "Hello", p2 = "World";
		
		exlog.logEx ( msgPrefix + " {}, {}", testEx, p1, p2 );

		var out = systemOutRule.getLog (); 

		assertTrue ( 
			"Error message not found!", 
			out.contains ( format ( "%s %s, %s. Error: %s", msgPrefix, p1, p2, testEx.getMessage () ) ) 
		);
		
		assertTrue (
			"Debug details not found!",
			out.contains ( format ( "%s %s, %s. Details:", msgPrefix, p1, p2 ) ) &&
			out.contains ( "java.lang.RuntimeException: " + testEx.getMessage () )
		);
		
	}
	

	/**
	 * Not so necessary (tested by default invocations), but just in case.
	 */
	@Test
	public void testCustomTails ()
	{
		var msgPrefix = "Testing the Runtime Exception Logger, with params:";
		String p1 = "Hello", p2 = "World";
		String errTailPrefix = ". The error is:", debugTail = ". The details are:";
		
		exlog.logEx ( msgPrefix + " {}, {}", errTailPrefix + " {}", debugTail, testEx, p1, p2 );

		var out = systemOutRule.getLog (); 

		assertTrue ( 
			"Error message not found!", 
			out.contains ( format ( 
				"%s %s, %s%s %s", 
				msgPrefix, p1, p2, errTailPrefix, testEx.getMessage () 
			)) 
		);
		
		assertTrue (
			"Debug details not found!",
			out.contains ( format ( "%s %s, %s%s", msgPrefix, p1, p2, debugTail ) ) &&
			out.contains ( "java.lang.RuntimeException: " + testEx.getMessage () )
		);	
	}

}