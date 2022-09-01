package uk.ac.ebi.utils.exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Aug 2022</dd></dl>
 *
 */
public class ExceptionLogger
{
	private final Logger log;
	private static final ExceptionLogger defaultExLog = new ExceptionLogger ();
	
	private ExceptionLogger () {
		this.log = LoggerFactory.getLogger ( this.getClass () ); 
	}
	
	private ExceptionLogger ( Logger logger )
	{
		this.log = logger;
	}
	
	private ExceptionLogger ( String name )
	{
		this ( LoggerFactory.getLogger( name ) );		
	}
	
	private ExceptionLogger ( Class<?> cls )
	{
		this ( LoggerFactory.getLogger( cls ) );
	}
	
	public static ExceptionLogger getLogger ( Logger logger ) 
	{
		return logger == null ? defaultExLog : new ExceptionLogger ( logger );
	}
	
	public static ExceptionLogger getLogger ( String name )
	{
		return new ExceptionLogger ( name );
	}
	
	public static ExceptionLogger getLogger ( Class<?> cls )
	{
		return new ExceptionLogger ( cls );
	}
	
	public static ExceptionLogger getLogger ()
	{
		return getLogger ( (Logger) null );
	}
	
	/**
	 * Defaults to reporting your message (instantiated with your params), plus sensibgle tails like
	 * ".Error: {}" for the error level entry and ".Details: {}" for the debug entry. 
	 * 
	 * @see #logEx(String, String, String, Throwable, Object...)
	 * 
	 */
	public void logEx ( String baseMessage,Throwable ex, Object... msgParams) 
	{
		logEx ( baseMessage, ". Error: {}", ". Details: {}", ex, msgParams );
	}
	
	/**
	 * Does the exception logging, as explained above.
	 *  
	 * @param baseMessage A base message. This can contain '{}' placeholders as usually. If it does, they must match 
	 *   msgParams.
	 *   
	 * @param errorMsgTail a tail that is used for the error level log message.
	 *   This usually contains a last '{}' placeholder, which
	 *   is filled with the {@link Throwable#getMessage() exception message}.
	 *   
	 * @param debugMsgTail a tail that is used for the debug level log. Similarly, this is filled with 
	 *   the {@link Throwable exception itself}, so that the entire stacktrace is reported. 
	 * 
	 * @param ex the exception that you want to log
	 * 
	 * @param msgParams the parameters to be used in the message placeholders. <b>WARNING</b>: as explained above, 
	 *   the final messsge is this plus errorMsgTail or debugMsgTail and the logger is invoked with this parameter
	 *   extended with either the exception messsge or the exception object. So you've to consider that
	 *   the last '{}' in the message (usually set in the tails) is for the exception, NOT for your 
	 *   parameters.
	 */
	public void logEx (
		String baseMessage, String errorMsgTail, String debugMsgTail, Throwable ex, Object... msgParams) 
	{
		Object [] out = new Object [ msgParams.length + 1 ];
		
		
		if ( log.isDebugEnabled() )
		{
		   System.arraycopy ( msgParams, 0, out, 0, msgParams.length );

		   if ( log.isErrorEnabled() )
		   {
		  	 // Usually it's cleaner to read the error level first
			   out [ msgParams.length ] = ex.getMessage ();
			   log.error ( baseMessage + errorMsgTail, out );
		   }
		   
		   // And then the debug message
		   out [ msgParams.length ] = ex;
		   log.debug ( baseMessage + debugMsgTail, out );
		}
		
	}
}