package uk.ac.ebi.utils.opt.springweb.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import uk.ac.ebi.utils.exceptions.ExceptionLogger;
import uk.ac.ebi.utils.opt.io.IOUtils;

/**
 * A better web exception handler.
 * 
 * This catches all exceptions happening while answering to a web requests (ie, in the controllers)
 * and return a proper HTTP status with a proper response body. Namely, the latter is
 * based on {@link #createProblemDetail(Exception, HttpStatusCode, String, String, Object[], WebRequest)},
 * which in turn, is based on <a href = "https://datatracker.ietf.org/doc/html/rfc9457">RFC-9457</a>.
 *
 * TODO: I've tested it in dependants only.
 * 
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>30 Aug 2024</dd></dl>
 *
 */
public class XResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{
	/**
	 * Maps known exceptions to HTTP statuses. This is scanned in the order you define the classes
	 * and that MUST be from the most specific one to the more generic.
	 * 
	 */
	@SuppressWarnings ( "serial" )
	protected Map<Class<? extends Exception>, HttpStatusCode> exception2StatusCode = new LinkedHashMap<> () 
	{{
		this.put ( SecurityException.class, HttpStatus.UNAUTHORIZED );
	}};
	
	/**
	 * If true, {@link #createProblemDetail(Exception, HttpStatusCode, String, String, Object[], WebRequest)}
	 * adds a 'trace' field to the RFC-9457 output all the exceptions in this class
	 * produce.
	 */
	protected boolean isStackTraceEnabled = true;
	
	private final ExceptionLogger exLog = ExceptionLogger.getLogger ( this.getClass () );

	/**
	 * Our own catch-all wrapper
	 * 
	 * This tries to see if the exception is associated to some HTTP status code, 
	 * by using {@link #findExceptionMapping(Exception)}. It then invokes
	 * {@link #handleExceptionInternal(Exception, Object, HttpHeaders, HttpStatusCode, WebRequest)}
	 * with the found status code (or null code).
	 *  
	 * This bypasses {@link #handleException(Exception, WebRequest)} and all the 
	 * defaults in the parent handler. We have tried the alternative route to call this
	 * method with a {@link ResponseStatusException} wrapper, but we don't want the latter
	 * to be returned to the client as top-level exception. Instead, we intercept
	 * occurred exceptions here and then we call {@link #handleExceptionInternal(Exception, Object, HttpHeaders, HttpStatusCode, WebRequest)}.
	 */
	@ExceptionHandler
	public ResponseEntity<Object> handleMappedException (
		Exception ex, WebRequest request
	)
	{
		var status = findExceptionMapping ( ex );
		return this.handleExceptionInternal ( ex, null, null, status, request );
	}
	
	/**
	 * All exceptions are eventually routed here.
	 * 
	 * <p>Does some tweaking of the default parent internal handler, before calling 
	 * it as delegate:
	 * 
	 * <ul>
	 *   <li>it always assign a status code, with 50x as fallback</li>
	 *   <li>if body is null, it uses {@link #createProblemDetail(Exception, HttpStatusCode, String, String, Object[], WebRequest)}
	 *   with the reason taken from statusCode (original or 50x fallback)</li>
	 * </p>
	 */
	@Nullable
	protected ResponseEntity<Object> handleExceptionInternal (
		Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request
	)
	{
		if ( statusCode == null || statusCode.value () < 400 )
			// We're dealing with an exception, so it cannot be less than that, override the original code
			statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
		
		if ( body == null )
		{
			// Error detail is required for ProblemDetail
			String errorDetail = Optional.ofNullable ( HttpStatus.valueOf ( statusCode.value () ) )
			.or ( () -> Optional.of ( HttpStatus.INTERNAL_SERVER_ERROR ) )
			.map ( HttpStatus::getReasonPhrase )
			.orElse ( "Error" );
			
			body = this.createProblemDetail (
				ex, statusCode, errorDetail, null, null, request
			);
		}
				
		exLog.logEx ( 
			"Returning exception from web request processing, HTTP status: {}",
			ex, statusCode 
		);
		
		return super.handleExceptionInternal ( ex, body, headers, statusCode, request );
	}
	
	/**
	 * Tweaks the original output to return more significant values:
	 * 
	 * - sets detail to the {@link uk.ac.ebi.utils.exceptions.ExceptionUtils#getSignificantMessage(Throwable) most significant message}
	 * from ex
	 * - sets title to the original detail (which isn't so detailed), discards the original title (since it has
	 * low information most of the times)
	 * - sets type to the FQN of ex
	 * - adds the 'trace' property with the exception's stack trace (TODO: make it optional) 
	 *
	 * As in the superclass, this is used by {@link #handleExceptionInternal(Exception, Object, HttpHeaders, HttpStatusCode, WebRequest)}.
	 */
	@Override
	protected ProblemDetail createProblemDetail (
		Exception ex, HttpStatusCode status, String defaultDetail, @Nullable String detailMessageCode,
		@Nullable Object[] detailMessageArguments, WebRequest request)
	{
		var result = super.createProblemDetail (
			ex, status, defaultDetail, detailMessageCode, detailMessageArguments, request
		);
		
		result.setType ( IOUtils.uri ( ex.getClass ().getCanonicalName () ) );
		// The old title is very minimal, detail is slightly better, so here we go
		result.setTitle ( result.getDetail () );
		result.setDetail ( uk.ac.ebi.utils.exceptions.ExceptionUtils.getSignificantMessage ( ex ) );
		
		if ( this.isStackTraceEnabled )
			result.setProperty ( "trace", ExceptionUtils.getStackTrace ( ex ) );
		
		return result;
	}
	
	
	/**
	 * Used within {@link #handleMappedException(Exception, WebRequest)}.
	 * It tries to find one class in exception2StatusCode that is the same class of ex or a parent of it.
	 * 
	 * @return the corresponding status if it finds something, null otherwise. 
	 * 
	 */
	protected HttpStatusCode findExceptionMapping ( Exception ex )
	{
		for ( var exClass: exception2StatusCode.keySet () )
		{
			if ( exClass.isAssignableFrom ( ex.getClass () ) )
				return exception2StatusCode.get ( exClass );
		}
		return null;
	}
}
