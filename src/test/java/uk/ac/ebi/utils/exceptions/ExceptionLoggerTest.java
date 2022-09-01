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

	/** This shows the typical way you're going to use it **/
	private final ExceptionLogger exlog = ExceptionLogger.getLogger ( getClass () );
	
	/** Not much need to change it all the time **/
	private final RuntimeException testEx = new RuntimeException ( "raised on purpose for testing" );

	
	@Test
	public void testBasics ()
	{
		
		exlog.logEx ( "Testing the Runtime Exception Logger",testEx );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[ERROR]: Testing the Runtime Exception Logger. Error: raised on purpose for testing" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime Exception Logger. Details:\n"
				+ "java.lang.RuntimeException: raised on purpose for testing" ) );

	}
	
	@Test
	public void testLogEx ()
	{
		exlog.logEx ( "Testing the Runtime exception", ": Custom Error :: {}", ": Custom Details ::", testEx);
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[DEBUG]: Testing the Runtime exception: Custom Details ::\n"
				+ "java.lang.RuntimeException: raised on purpose for testing" ) );
		
		Assert.assertTrue ( systemOutRule.getLog().contains ( "[ERROR]: Testing the Runtime exception: Custom Error :: raised on purpose for testing" ) );
	}
	
}