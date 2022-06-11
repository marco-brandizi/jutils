package uk.ac.ebi.utils.exceptions;

/**
 * To report that a component or method cannot be used at moment, 
 * 
 * since it has to complete some operation and it's not ready yet. In particular, this can be useful to manage 
 * asynchronous initialisation. When an invoker receives this exception, it should expect that the component
 * becomes ready later on.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jan 2021</dd></dl>
 *
 */
public class NotReadyException extends IllegalStateException
{
	private static final long serialVersionUID = -7112790967925493354L;

	public NotReadyException ( String message, Throwable cause ) {
		super ( message, cause );
	}

	public NotReadyException ( String message ) {
		super ( message );
	}
}
