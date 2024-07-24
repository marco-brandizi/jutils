package uk.ac.ebi.utils.exceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * Utilities related to exception handling.
 *
 * <dl><dt>date</dt><dd>2 Oct 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ExceptionUtils
{
	private ExceptionUtils () {}
	
	/**
	 * Gets the root cause of an exception. To do that, it traces back the hierarchy of the exception's 
	 * {@link Throwable#getCause() causes}, until it finds an exception that has no upper cause.
	 * 
	 * Eventually, it returns the first exception that has no cause attached and hence it always returns
	 * a non-null result.
	 * 
	 * @throws NullPointerException if ex is null.
	 * 
	 */
	public static Throwable getRootCause ( Throwable ex )
	{
		if ( ex == null ) throw new NullPointerException ( "Cannot compute the root cause of a null exception" );
				
		for ( Throwable cause; ; ex = cause )
			if ( ( cause = ex.getCause () ) == null ) return ex;
	}
	
	/**
	 * Goes through the {@link Throwable#getCause() exception's hierarchy} until it finds one that has a non-null
	 * {@link Throwable#getMessage() message}.
	 * 
	 * This might return the initial exception itself. If none of the exceptions in the exception hierarchy has a 
	 * non-null message, the method returns null.
	 * 
	 * @throws NullPointerException if ex is null.
	 * 
	 */
	public static Throwable getSignificantException ( Throwable ex )
	{
		if ( ex == null ) throw new NullPointerException ( "Cannot compute the significant exception for a null exception" );

		for ( ; ex != null; ex = ex.getCause () )
			if ( ex.getMessage () != null ) return ex;
		
		return null;
	}
	
	/**
	 * Takes the result that {@link #getSignificantException(Throwable)} returns and then
	 * returns its {@link Throwable#getMessage()}. Returns null if the significant exception is null.
	 * 
	 */
	public static String getSignificantMessage ( Throwable ex )
	{
		return Optional.ofNullable ( getSignificantException ( ex ) )
		.map ( Throwable::getMessage )
		.orElse ( null );
	}
	
	
	/**
	 * Helper to ease the building of an exception and its message.
	 *  
	 * Builds an exception instance of exType, with the given message template instantiated with
	 * the given parameters.
	 * 
	 * The message template is passed to {@link String#format(String, Object...)}, 
	 * so you have to use the printf-style rules (eg, '%s' for a string parameter).
	 * 
	 * Additionally, you can use {@code $cause} or <tt>${cause}</tt> to have the 
	 * {@link Throwable#getMessage() cause's message} reported.  
	 * 
	 * The new exception is assigned a cause, if the corresponding parameter is non-null.
	 * 
	 */
	public static <E extends Throwable> E buildEx (
		Class<E> exType, Throwable cause, String messageTemplate, Object... params
	)
	{		
		String msg = "<null>"; 
		
		try
		{
			msg = String.format ( messageTemplate, params );
			
			if ( cause != null )
			{
				String causeMsg = StringUtils.trimToEmpty ( getSignificantMessage ( cause ) );
				causeMsg = Matcher.quoteReplacement ( causeMsg );
				
				msg = msg.replaceAll ( "(\\$cause|\\$\\{cause\\})", causeMsg );
				
				// Some exception wrappers like UncheckedIOException have constructors with mandatory cause
				Constructor<E> constructor = ConstructorUtils.getMatchingAccessibleConstructor ( exType, String.class, cause.getClass () );
				if ( constructor != null ) return constructor.newInstance ( msg, cause );
				
				constructor = ConstructorUtils.getMatchingAccessibleConstructor ( exType, String.class );
				if ( constructor != null )
				{
					E result = constructor.newInstance ( msg );
					if ( result.getCause () == null ) result.initCause ( cause );
					return result;
				}
					
				// Maybe some don't even accept a message? Unlikely, but just in case
				constructor = ConstructorUtils.getMatchingAccessibleConstructor ( exType, cause.getClass () );
				if ( constructor != null ) return constructor.newInstance ( cause );
				
				// Neither a message, nor a cause?! Hope it will never happen
				E result = ConstructorUtils.invokeConstructor ( exType );
				if ( result.getCause () == null ) result.initCause ( cause );
				return result;
			}
			
			// Now, all the cases without a cause
			//
			
			Constructor<E> constructor = ConstructorUtils.getMatchingAccessibleConstructor ( exType, String.class );
			if ( constructor != null ) return constructor.newInstance ( msg );
			
			// Again, the unlikely case where it doesn't accept a message					
			return ConstructorUtils.invokeConstructor ( exType );
		}
		catch ( NoSuchMethodException | SecurityException | InstantiationException 
						| IllegalAccessException | IllegalArgumentException | InvocationTargetException ex )
		{
			throw buildEx ( 
				IllegalArgumentException.class,
				ex,
				"Error while throwing exception for the message \"%s\": %s",
				msg,
				ex.getMessage ()
			);
		}
	}
	
	/**
	 * Wrapper with no cause.
	 * 
	 */
	public static <E extends Throwable> E buildEx (
		Class<E> exType, String messageTemplate, Object... params
	)
	{
		return buildEx ( exType, null, messageTemplate, params );
	}
	
	/**
	 * Wrapper that uses {@code cause.getClass()} as exception type.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <E extends Throwable> E buildEx (
		E cause, String messageTemplate, Object... params
	) throws E
	{
		return buildEx ( (Class<E>) cause.getClass (), cause, messageTemplate, params );
	}

	
	/**
	 * This calls {@link #buildEx(Class, Throwable, String, Object...)} and then throws the built exception. 
	 * Note that Java will consider this method as throwing checked/unchecked exception code, depending on
	 * the type of E.
	 * 
	 * Note that you cannot always use this to wrap the body of a function, since, in this case 
	 * Java will not know that you're certainly throwing an exception and hence it will think 
	 * that you are not returning any value after the catch clause that uses this method. If you are in this 
	 * situation, use {@code throw buildEx (...)} instead.
	 *  
	 */
	public static <E extends Throwable> void throwEx (
		Class<E> exType, Throwable cause, String messageTemplate, Object... params
	) throws E
	{
		throw buildEx ( exType, cause, messageTemplate, params );
	}	

	/**
	 * A wrapper with no cause.
	 */
	public static <E extends Throwable> void throwEx (
		Class<E> exType, String messageTemplate, Object... params
	) throws E
	{
		throwEx ( exType, null, messageTemplate, params );
	}
	
	/**
	 * Wrapper that uses {@code cause.getClass()} as exception type.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <E extends Throwable> void throwEx (
		E cause, String messageTemplate, Object... params
	) throws E
	{
		throwEx ( ( Class<E> ) cause.getClass (), cause, messageTemplate, params );
	}
}
