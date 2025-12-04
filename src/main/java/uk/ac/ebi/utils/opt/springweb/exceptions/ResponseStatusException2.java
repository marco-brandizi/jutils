package uk.ac.ebi.utils.opt.springweb.exceptions;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ResponseStatusException;

/**
 * An alternative version of {@link ResponseStatusException} that yields a more compact and better-formatted
 * {@link #getMessage()}. Throw this in place of {@link ResponseStatusException} if you prefer our flavour of it.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>9 Jun 2021</dd></dl>
 *
 */
public class ResponseStatusException2 extends ResponseStatusException
{

	private static final long serialVersionUID = 8006549394634362306L;

	public ResponseStatusException2 ( HttpStatus status )
	{
		super ( status );
	}

	public ResponseStatusException2 ( HttpStatus status, String reason )
	{
		super ( status, reason );
	}

	public ResponseStatusException2 ( HttpStatus status, String reason, Throwable cause )
	{
		super ( status, reason, cause );
	}

	public ResponseStatusException2 ( int rawStatusCode, String reason, Throwable cause )
	{
		super ( rawStatusCode, reason, cause );
	}

	/**
	 * Returns something like: 
	 * 
	 * "User not found (HTTP 404 NOT_FOUND)"
	 * 
	 * The first part is {@link #getReason()}, if null, it tries to get {@link HttpStatus#getReasonPhrase()} from
	 * {@link #getStatus()}, if this is null too, the second part only is reported without any braces.
	 * 
	 * If {@link #getRawStatusCode()} only is available, the second part reports the int code only.
	 * 
	 * This version of the message getter doesn't yield the cause's message and this is up to you 
	 * to pass it in the constructor.
	 * 
	 */
	@Override
	public @NonNull String getMessage ()
	{
		HttpStatusCode statusCode = Optional.ofNullable ( this.getStatusCode () )
				.orElse ( HttpStatus.INTERNAL_SERVER_ERROR );
		
		int code = statusCode.value ();
		
		// Finding a reason with Spring >= 6
		HttpStatus status = HttpStatus.resolve ( code );
		
		String codeStr = "HTTP " + 
			Optional.ofNullable ( status )
			.map ( HttpStatus::toString )
			.orElse ( "" + code );
		
		String msg = Optional.ofNullable ( this.getReason () )
		.or ( () -> Optional.ofNullable ( status ).map ( HttpStatus::getReasonPhrase ) )
		.map ( m -> m + " (" + codeStr + ")" )
		.orElse ( codeStr );
		
		return msg;
	}
}
