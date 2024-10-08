package uk.ac.ebi.utils.exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An helper to log exceptions with double level log events, error and debug.
 * 
 * <p>This is a simple utility to log an exception by producing one log event at 'error' level that only
 * shows a user-provided message and reports the {@link Throwable#getMessage() exception's message}, followed
 * by a debug event that shows the same user message plus the exception's 
 * {@link Throwable#getStackTrace() stack trace}. As you can imagine this is useful when you have log targets 
 * with different granularity reporting, eg, a console log at 'warning' level, plus a 'detailed.log' file at
 * debug level.</p>
 * 
 * <p>{@code getLogger()} methods are provided, so that you can use this class the same way common logging
 * libraries are used, eg, define a field like {@code exlog = ExceptionLogger.getLogger(...)} and then use
 * it in the class code.</p>
 * 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Aug 2022</dd></dl>
 *
 */
public class ExceptionLogger
{
	private final Logger log;
	private static final ExceptionLogger DEFAULT_EX_LOG = new ExceptionLogger ();
	
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
	
	/**
	 * If null, uses a default that is based on {@link ExceptionLogger#getClass()}.
	 * @see LoggerFactory
	 */
	public static ExceptionLogger getLogger ( Logger logger ) 
	{
		return logger == null ? DEFAULT_EX_LOG : new ExceptionLogger ( logger );
	}
	
	public static ExceptionLogger getLogger ( String name )
	{
		return new ExceptionLogger ( name );
	}
	
	public static ExceptionLogger getLogger ( Class<?> cls )
	{
		return new ExceptionLogger ( cls );
	}
	
	/**
	 * The default logger, see {@link #getLogger()}.
	 */
	public static ExceptionLogger getLogger ()
	{
		return getLogger ( (Logger) null );
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
	 * @param debugMsgTail a tail that is used for the debug level log. This usually hasn't any placeholder,
	 *   since we automatically add the exception ex to the {@link Logger#debug(String, Object...)} and this
	 *   reports the stacktrace, no matter if you have used a placeholder in the message or not.
	 * 
	 * @param ex the exception that you want to log
	 * 
	 * @param msgParams the parameters to be used in the message placeholders. <b>WARNING</b>: as explained above, 
	 *   the final message for the logger is baseMessage plus errorMsgTail or debugMsgTail, and the logger is invoked 
	 *   with msgParams extended with either the exception message or the exception object. So, you've to consider that
	 *   the last '{}' in the message (usually set in the tails) is for the exception message, NOT for your 
	 *   parameters. Moreover, the debug message reports the exception details anyway, as mentioned above.
	 */
	public void logEx (
		String baseMessage, String errorMsgTail, String debugMsgTail, Throwable ex, Object... msgParams 
	) 
	{
		// If error isn't active, then debug level isn't either, so let's save a few CPU cycles 
		// by checking error first.
		
		if ( log.isErrorEnabled () )
		{
			/*
			 * We manage the case of no parameters separately, since that can be done more efficiently than 
			 * the general case.
			 */

			if ( msgParams == null || msgParams.length == 0 )
			{
				// Usually it's cleaner to read the error level first
				log.error ( baseMessage + errorMsgTail, ex.getMessage () );

				// So, debug message comes afterwards
				if ( log.isDebugEnabled () ) 
					log.debug ( baseMessage + debugMsgTail, ex );

				return;
			}

			// we actually have params, let's deal with them
			//

			Object[] out = new Object[ msgParams.length + 1 ];

			System.arraycopy ( msgParams, 0, out, 0, msgParams.length );

			// Again, let's report in the naturally expected order
			out[ msgParams.length ] = ex.getMessage ();
			log.error ( baseMessage + errorMsgTail, out );

			if ( log.isDebugEnabled () )
			{
				// And then the debug message
				out[ msgParams.length ] = ex;
				log.debug ( baseMessage + debugMsgTail, out );
			}
		} // if isError
	} // logEx()
	
	/**
	 * Defaults to reporting your message (instantiated with your params), plus sensible tails like
	 * ". Error: {}" for the error level entry and ". Details:" for the debug entry. As explained, 
	 * we don't need a placeholder for the exception stacktrace. since SLF4J reports it anyway. 
	 * 
	 * @see #logEx(String, String, String, Throwable, Object...)
	 * 
	 */
	public void logEx ( String baseMessage, Throwable ex, Object... msgParams) 
	{
		logEx ( baseMessage, ". Error: {}", ". Details:", ex, msgParams );
	}
	
}