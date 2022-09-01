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
	
	public void logEx ( String baseMessage,Throwable ex, Object... msgParams) 
	{
		logEx ( baseMessage, ". Error {}", ". Details {}", ex, msgParams );
	}
	
	public void logEx ( String baseMessage, String errorMsgTail, String debugMsgTail, Throwable ex,
		Object... msgParams) 
	{
		Object [] out = new Object [ msgParams.length + 1 ];
		
		
		if ( log.isDebugEnabled() )
		{
		   System.arraycopy ( msgParams, 0, out, 0, msgParams.length );
		   out [ msgParams.length ] = ex;
		   log.debug ( baseMessage + debugMsgTail, out );
		   if ( !log.isErrorEnabled() ) {
			   return;
		   }
		   out [ msgParams.length ] = ex.getMessage ();
		   log.error ( baseMessage + errorMsgTail, out );
		}
		
	}
}