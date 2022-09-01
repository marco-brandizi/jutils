package uk.ac.ebi.utils.exceptions;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

/**
 *
 * @author joji c unnunni
 * <dl><dt>Date:</dt><dd>22 Aug 2022</dd></dl>
 *
 */
public class ExceptionLoggerTest
{
	@Rule
	private final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

	/** This shows the typical way you're going to use it **/
	private final ExceptionLogger exlog = ExceptionLogger.getLogger ( getClass () );
	
	/** Not much need to change it all the time **/
	private final RuntimeException testEx = new RuntimeException ( "raised on purpose for testing" );

	
	@Test
	public void testBasics ()
	{
		exlog.logEx ( "Testing the Runtime Exception Logger param: {}", testEx, 10.5 );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime Exception Logger. Details 10.5" ) );
		Assert.assertTrue ( systemOutRule.getLog().contains ( "java.lang.RuntimeException: A runtime test exception with defaults" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[ERROR]: Testing the Runtime Exception Logger. Error 10.5" ) );

	}
	
	@Test
	public void testLogEx ()
	{
		exlog.logEx ( "Testing the Runtime exception", " Error without param", " Details without param", testEx);
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime exception Details without param" ) );
		Assert.assertTrue ( systemOutRule.getLog().contains ( "java.lang.RuntimeException: A runtime test exception with Error and Details message" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[ERROR]: Testing the Runtime exception Error without param" ) );
	}
	
	@Test
	public void testLogExParams ()
	{
		exlog.logEx ( "Testing the Runtime exception", ". Error param {}", ". Details param {}", testEx, "ERROR",100);
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime exception. Details param ERROR" ) );
		Assert.assertTrue ( systemOutRule.getLog().contains ( "java.lang.RuntimeException: A runtime test exception with Error and Details message" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "ERROR]: Testing the Runtime exception. Error param ERROR" ) );
	}
	
}