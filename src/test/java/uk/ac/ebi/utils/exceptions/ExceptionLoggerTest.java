package uk.ac.ebi.utils.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author joji c unnunni
 * <dl><dt>Date:</dt><dd>22 Aug 2022</dd></dl>
 *
 */
public class ExceptionLoggerTest
{
	
	
	@Test
	public void testLogExWithDebug ()
	{
		RuntimeException ex = ExceptionUtils.buildEx ( 
			RuntimeException.class, "A test exception with param having value: %.1f param", 10.5 
		);
		
		Object [] param= {10.5};
		Object[] init = initConsole ();
		
		setLoggingLevel(ch.qos.logback.classic.Level.DEBUG);
		
		ExceptionLogger.getLogger ( getClass () ).logEx ( "Testing the Runtime exception", " error {}", " details {}", ex,
				param );
		String cons = readFromConsole ( init );
		Assert.assertTrue ( cons.contains ( "[DEBUG]: Testing the Runtime exception details 10.5" ) );
		
		Assert.assertTrue ( cons.contains ( "java.lang.RuntimeException: A test exception with param having value: 10.5 param" ) );
	}
	
	@Test
	public void testLogExWithError ()
	{
		RuntimeException ex = ExceptionUtils.buildEx ( 
			RuntimeException.class, "A test exception with param having value: %.1f param", 10.5 
		);
		
		Object [] param = { };
		Object [] init = initConsole ();
		
		setLoggingLevel ( ch.qos.logback.classic.Level.ERROR );
		
		ExceptionLogger.getLogger ( getClass () ).logEx ( "Testing the Runtime exception", " error {}", " details {}", ex,
				param );
		String cons = readFromConsole ( init );
		Assert.assertTrue ( cons.contains ( "[ERROR]: Testing the Runtime exception error A test exception with param having value: 10.5 param" ) );
	}
	
	Object [] initConsole () 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream ();
		PrintStream ps = new PrintStream ( baos );
		PrintStream old = System.out;
		System.setOut ( ps );
		Object [] out = { baos,old };
		return out;
	}
	
	String readFromConsole ( Object [] obj )
	{
		System.out.flush ();
		System.setOut ( ( PrintStream ) obj [1] );
		return obj [0].toString ();
	}
	
	
	public static void setLoggingLevel ( ch.qos.logback.classic.Level level ) 
	{
		ch.qos.logback.classic.Logger root = ( ch.qos.logback.classic.Logger ) org.slf4j.LoggerFactory
				.getLogger ( ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME );
		root.setLevel ( level );
	}
	
	
}
