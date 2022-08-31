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
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

	
	@Test
	public void testBasics ()
	{
		
		RuntimeException ex = new RuntimeException ( "A runtime test exception with defaults" );
		
		ExceptionLogger.getLogger ( getClass () ).logEx ( "Testing the Runtime Exception Logger", ex, 10.5 );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime Exception Logger. Details 10.5" ) );
		Assert.assertTrue ( systemOutRule.getLog().contains ( "java.lang.RuntimeException: A runtime test exception with defaults" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[ERROR]: Testing the Runtime Exception Logger. Error 10.5" ) );

	}
	
	@Test
	public void testLogEx ()
	{
		RuntimeException ex = new RuntimeException ( "A runtime test exception with Error and Details message" );
		
		ExceptionLogger.getLogger ( getClass () ).logEx ( "Testing the Runtime exception", " Error without param", " Details without param", ex);
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime exception Details without param" ) );
		Assert.assertTrue ( systemOutRule.getLog().contains ( "java.lang.RuntimeException: A runtime test exception with Error and Details message" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[ERROR]: Testing the Runtime exception Error without param" ) );
	}
	
	@Test
	public void testLogExParams ()
	{
		RuntimeException ex = new RuntimeException ( "A runtime test exception with Error and Details message" );
		
		ExceptionLogger.getLogger ( getClass () ).logEx ( "Testing the Runtime exception", ". Error param {}", ". Details param {}", ex, "ERROR",100);
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime exception. Details param ERROR" ) );
		Assert.assertTrue ( systemOutRule.getLog().contains ( "java.lang.RuntimeException: A runtime test exception with Error and Details message" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "ERROR]: Testing the Runtime exception. Error param ERROR" ) );
	}
}