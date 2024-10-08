package uk.ac.ebi.utils.opt.xml;

import java.io.InputStream;

import org.unix4j.Unix4j;
import org.unix4j.builder.Unix4jCommandBuilder;

import uk.ac.ebi.utils.opt.io.Unix4jUtils;

/**
 * Utilities to filter XML streams and alike.
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>22 Mar 2018</dd></dl>
 *
 */
public class XmlFilterUtils
{
	/**
	 * <p>Wraps the text content of any element with one of the tags in the parameter with 
	 * {@code <![CDATA[...]]>}. This is done on a regular input stream, so that an XML parser can deal with
	 * wrong XML without failing. This is also done dynamically, the returning stream is processed 
	 * while reading and we don't stuff memory with all the data coming from the original stream (but you may want
	 * to buffer it).</p>
	 * 
	 * <p><b>WARNING</b>: In order to avoid too many dependency for jUtils, you have to declare the dependency on
	 * both Unix4j and EasyStream.</p>
	 * 
	 * <b>TODO</b>: this doesn't work when tags are nested, like in:
	 * {@code <foo>bla bla bla <foo> bla </foo>}. In this case, the inner foo will have a CDATA block appended.
	 * 
	 */
	public static InputStream cdataWrapper ( InputStream xmlin, String... tags )
	{
		// First substitution: <Tag> (but not <Tag/>, nor <Tag1>) is replaced by <Tag><![CDATA[
		// This also should work for <Tag attr = '...' >(and the self-closing version), but 
		// might have problems with '/' inside the attribute value.
		// 
		// \<(ArticleTitle|AbstractText)(\s[^\>]+|)(?<!\/)\> => \<\1\2\>\<\!\[CDATA\[
		// <TAG...>, only if '>' is not preceded by '/'
		String tagStr = String.join ( "|", tags );
		String re = "\\<(" + tagStr + ")(\\s[^\\>]+|)(?<!\\/)\\>";
		// $1 is the tag, $2 is anything after <Tag The expression used for it excludes <TagXyz> 
		String repl = "\\<$1$2\\>\\<\\!\\[CDATA\\[";
		String sedOpen = "s/" + re + "/" + repl + "/g";
		
		// Second substitution: all the closing tags like </Tag>) are replaced by ]]></Tag>
		//
		// \<\/(ArticleTitle|AbstractText)\> => \]\]\>\<\/\1\>		
		re = "\\<\\/(" + tagStr + ")\\>";
		repl = "\\]\\]\\>\\<\\/$1\\>";
		String sedClose = "s/" + re + "/" + repl + "/g";

		// Chain and return it as a filter
		Unix4jCommandBuilder sedCmd = Unix4j.from ( xmlin ).sed ( sedOpen ).sed ( sedClose );
		return Unix4jUtils.unixFilter ( sedCmd );
	}
}
