package uk.ac.ebi.utils.opt.net.exceptions;

import org.apache.hc.core5.http.HttpException;

/**
 * An unchecked verision of {@link HttpException}
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>24 May 2022</dd></dl>
 *
 */
public class UncheckedHttpException extends RuntimeException
{
	private static final long serialVersionUID = -7130488860665860470L;

	public UncheckedHttpException ( String message, HttpException cause )
	{
		super ( message, cause );
	}
}
