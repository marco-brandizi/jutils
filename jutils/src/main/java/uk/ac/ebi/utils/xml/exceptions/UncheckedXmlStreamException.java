package uk.ac.ebi.utils.xml.exceptions;

import javax.xml.stream.XMLStreamException;

import uk.ac.ebi.utils.exceptions.UnexpectedValueException;

/**
 * Unchecked version of {@link XMLStreamException}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Jul 2020</dd></dl>
 *
 */
public class UncheckedXmlStreamException extends UnexpectedValueException
{
	private static final long serialVersionUID = 1679739550040163976L;

	public UncheckedXmlStreamException ( String message, XMLStreamException cause )
	{
		super ( message, cause );
	}

	public UncheckedXmlStreamException ( String message )
	{
		super ( message );
	}

}
