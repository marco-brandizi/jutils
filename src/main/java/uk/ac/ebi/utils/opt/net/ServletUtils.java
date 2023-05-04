package uk.ac.ebi.utils.opt.net;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import uk.ac.ebi.utils.exceptions.ExceptionUtils;

/**
 * Utilities about the HTTP protocol and the respective Java packages.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>4 May 2023</dd></dl>
 *
 */
public class ServletUtils
{
	/**
	 * Little helper to extract a string from {@link HttpResponse#getEntity()}, 
	 * reading from {@link HttpEntity#getContent()}.
	 * 
	 * @return the response body, or the empty string, if response's entity is null.
	 * 
	 * TODO: write a test.
	 */
	public static String getResponseBody ( HttpResponse response )
	{
		try
		{
			HttpEntity responseEntity = response.getEntity ();
			if ( responseEntity == null ) return "";
			return IOUtils.toString ( responseEntity.getContent (), Charset.forName ( "UTF-8" ) );
		}
		catch ( UnsupportedOperationException | IOException ex )
		{
			throw ExceptionUtils.buildEx ( 
				UncheckedIOException.class, ex,
				"Error while retrieving HTTP response body: $cause"
			);
		}
	}
}
