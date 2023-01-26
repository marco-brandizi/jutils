package uk.ac.ebi.utils.opt.xml;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Mar 2018</dd></dl>
 *
 */
public class XmlFilterUtilsTest
{
	Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testCdataWrapper () throws IOException
	{
		String xml = 
			"<Abstract/>\n" + 
			"<ArticleTitle>test</ArticleTitle>\n" + 
			"<Abstract id = '1' />\n" + 
			"<ArticleTitle id = '2'>test1</ArticleTitle>\n" + 
			"<Abstract foo = '/3' >test2</Abstract>\n" +  
			"<Abstract foo = '/4' />\n"; 
		
		ReaderInputStream xmlin = new ReaderInputStream ( new StringReader ( xml ), "UTF-8" );
		
		InputStream xmlw = XmlFilterUtils.cdataWrapper ( xmlin, "ArticleTitle", "Abstract" );
		String outs = IOUtils.toString ( xmlw, "UTF-8" );
		
		log.info ( "Resulting XML fragment:\n{}", outs );
		
		assertTrue ( "Wrong result for self-closing tag!", outs.startsWith ( "<Abstract/>\n" ) );
		assertTrue ( "Wrong result!", outs.contains ( "<ArticleTitle><![CDATA[test]]></ArticleTitle>\n" ) );
		
		assertTrue ( "Wrong result for self-closing tag + attrib!", outs.contains ( "<Abstract id = '1' />\n" ) );
		assertTrue (
			"Wrong result for tag + attrib!", 
			outs.contains ( "<ArticleTitle id = '2'><![CDATA[test1]]></ArticleTitle>\n" ) 
		);		

		// Tricky case with '/' inside the attrib value
		assertTrue (
			"Wrong result for tag + attrib + '/'!", 
			outs.contains ( "<Abstract foo = '/3' ><![CDATA[test2]]></Abstract>\n" ) 
		);		
		assertTrue (
			"Wrong result for self-closing tag + attrib + '/'!", 
			outs.contains ( "<Abstract foo = '/4' />\n" ) 
		);
	}
	
	/**
	 * Must wrap {@code <Citation>}, not {@code <CitationSet>}
	 * @throws IOException
	 */
	@Test
	public void testCdataWrapperSamePrefixTags () throws IOException
	{
		String xml = 
			"<CitationSet>\n" + 
			"  <Citation>test2</Citation>\n" +
			"</CitationSet>\n";
		
		ReaderInputStream xmlin = new ReaderInputStream ( new StringReader ( xml ), "UTF-8" );
		
		InputStream xmlw = XmlFilterUtils.cdataWrapper ( xmlin, "Citation" );
		String outs = IOUtils.toString ( xmlw, "UTF-8" );
		log.info ( "Resulting XML fragment:\n{}", outs );
		
		assertTrue (
			"Wrong result for <Citation> inner tag!", 
			outs.contains ( "<Citation><![CDATA[test2]]></Citation>\n" )
		);

		assertFalse ( "<CitationSet> was wrapped!", outs.contains ( "<CitationSet><![CDATA[" ) );
		assertFalse ( "<CitationSet> was wrapped!", outs.contains ( "]]></CitationSet>" ) );

		xml = 
			"<CitationSet n = '1'>\n" + 
			"  <Citation  i=\"true\"></Citation>\n" +
			"</CitationSet>\n";
		
		xmlin = new ReaderInputStream ( new StringReader ( xml ), "UTF-8" );
		xmlw = XmlFilterUtils.cdataWrapper ( xmlin, "Citation" );
		outs = IOUtils.toString ( xmlw, "UTF-8" );
		log.info ( "Another XML fragment:\n{}", outs );
		
		assertTrue (
			"Wrong result for <Citation> inner tag with attributes!", 
			outs.contains ( "<Citation  i=\"true\"><![CDATA[]]></Citation>\n" )
		);

		assertFalse ( 
			"<CitationSet> with attribute was wrapped!",
			outs.contains ( "<CitationSet n = '1'><![CDATA[" )
		);
	}	
}
