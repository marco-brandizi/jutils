package uk.ac.ebi.utils.xml.stax;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Utilities to manage the Stax parser API.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Jul 2020</dd></dl>
 *
 */
public class StaxUtils
{
	private StaxUtils () {}
	
	/**
	 * Invokes {@link XMLStreamReader#nextTag()} and checks that then: we are in a 
	 * {@link XMLStreamConstants#START_ELEMENT}, the element is {@code tag}.
	 * 
	 */
	public static void expectNextTag ( XMLStreamReader xmlr, String tag ) 
		throws XMLStreamException
	{
		if ( xmlr.nextTag () != START_ELEMENT ) throw new XMLStreamException ( 
			"Expecting start tag '" + tag + "' at " + xmlCoord ( xmlr)
		);
		
		String docTag = xmlr.getLocalName ();
		if ( !tag.equals ( docTag ) ) throw new XMLStreamException ( 
			"Expecting start tag '" + tag + "' at " + xmlCoord ( xmlr) + ", found '" + docTag + "' instead"
		);
	}
	
	/**
	 * Invokes {@link #expectNextTag(XMLStreamReader, String)} and then 
	 * {@link XMLStreamReader#getElementText()}.
	 */
	public static String readNextTag ( XMLStreamReader xmlr, String tag ) throws XMLStreamException
	{
		expectNextTag ( xmlr, tag );
		return xmlr.getElementText();
	}

	/**
	 * Renders {@link Location} in a human-readable format, useful in logging and error messages.
	 */
	public static String xmlCoord ( Location xmlLoc )
	{
		return "line: " + xmlLoc.getLineNumber () + ", col: " + xmlLoc.getColumnNumber ();
	}

	/**
	 * Wrapper of {@link XMLStreamReader#getLocation()}.
	 */
	public static String xmlCoord ( XMLStreamReader xmlr )
	{
		return xmlCoord ( xmlr.getLocation () );
	}

}
