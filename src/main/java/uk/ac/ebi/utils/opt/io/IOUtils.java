/*
 * __________
 * CREDITS
 * __________
 *
 * Team page: http://isatab.sf.net/
 * - Marco Brandizi (software engineer: ISAvalidator, ISAconverter, BII data management utility, BII model)
 * - Eamonn Maguire (software engineer: ISAcreator, ISAcreator configurator, ISAvalidator, ISAconverter,  BII data management utility, BII web)
 * - Nataliya Sklyar (software engineer: BII web application, BII model,  BII data management utility)
 * - Philippe Rocca-Serra (technical coordinator: user requirements and standards compliance for ISA software, ISA-tab format specification, BII model, ISAcreator wizard, ontology)
 * - Susanna-Assunta Sansone (coordinator: ISA infrastructure design, standards compliance, ISA-tab format specification, BII model, funds raising)
 *
 * Contributors:
 * - Manon Delahaye (ISA team trainee:  BII web services)
 * - Richard Evans (ISA team trainee: rISAtab)
 *
 *
 * ______________________
 * Contacts and Feedback:
 * ______________________
 *
 * Project overview: http://isatab.sourceforge.net/
 *
 * To follow general discussion: isatab-devel@list.sourceforge.net
 * To contact the developers: isatools@googlegroups.com
 *
 * To report bugs: http://sourceforge.net/tracker/?group_id=215183&atid=1032649
 * To request enhancements:  http://sourceforge.net/tracker/?group_id=215183&atid=1032652
 *
 *
 * __________
 * License:
 * __________
 *
 * This work is licenced under the Creative Commons Attribution-Share Alike 2.0 UK: England & Wales License. To view a copy of this licence, visit http://creativecommons.org/licenses/by-sa/2.0/uk/ or send a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco, California 94105, USA.
 *
 * __________
 * Sponsors
 * __________
 * This work has been funded mainly by the EU Carcinogenomics (http://www.carcinogenomics.eu) [PL 037712] and in part by the
 * EU NuGO [NoE 503630](http://www.nugo.org/everyone) projects and in part by EMBL-EBI.
 */
package uk.ac.ebi.utils.opt.io;

import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.machinezoo.noexception.Exceptions;

import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;
import uk.ac.ebi.utils.threading.HackedBlockingQueue;



/**
 * Miscellanea of small IO utilities.
 * 
 * TODO: complete the migration to unchecked exceptions.
 * TODO: does it need to be an *.opt.* package?
 * 
 * <dl><dt>date</dt><dd>July 29, 2007, 1:03 PM</dd></dl>
 * @author brandizi
 *
 */
public class IOUtils 
{
	private static Logger log = LoggerFactory.getLogger ( IOUtils.class );
	
	private IOUtils () {}
		
	/**
	 * @see #readFile(String, Charset)
	 */
	public static String readFile ( String path, String charSet ) throws UncheckedIOException {
		return readFile ( path, Charset.forName ( charSet ) );
	}

	/**
	 * Opens an input stream from a file path.
	 */
	public static String readFile ( String path, Charset charSet ) throws UncheckedIOException
	{
		try {
			return Files.readString ( Path.of ( path ), charSet );
		}
		catch ( IOException ex )
		{
			throw buildEx ( UncheckedIOException.class, ex, 
				"Error while reading from \"%s\": $cause", path 
			);
		}
	}

	
	/**
	 * Defaults to UTF-8
	 */
	public static String readFile ( String path ) throws UncheckedIOException { 
		return readFile ( path, UTF_8 );
	}

	/**
	 * Invokes {@link #readFile(String, Charset)} upon all the files in a directory and return an array of
	 * file contents. 
	 */
	public static String[] readFiles ( String dirPath, FilenameFilter filter, boolean ignoreMissingDir, Charset charSet )
	  throws UncheckedFileNotFoundException, UncheckedIOException
	{
		File dir = new File ( dirPath );
		if ( !dir.isDirectory () ) 
		{
			if ( ignoreMissingDir ) {
				log.warn ( "readFiles(), ignoring missing directory '{}'", dirPath );
				return new String [ 0 ]; 
			}
			throwEx ( UncheckedFileNotFoundException.class, "Directory '%s' not found", dirPath );
		}
		
		File[] files = filter == null ? dir.listFiles () : dir.listFiles ( filter );
		if ( files == null ) return new String [ 0 ];
		
		String[] result = new String [ files.length ];
		for ( int i = 0; i < files.length; i++ )
		{
			Path path = files [ i ].toPath ().toAbsolutePath ();
			try {
				result [ i ] = Files.readString ( path, charSet );
			}
			catch ( IOException ex )
			{
				throwEx ( UncheckedIOException.class, ex, 
					"Error while reading file \"%s\": $cause", path.toString ()
				);
			}
		}
		return result;
	}
	
	/**
	 * Defaults to missingDir = false
	 */
	public static String[] readFiles ( String dirPath, FilenameFilter filter, Charset charSet ) throws UncheckedIOException 
	{
		return readFiles ( dirPath, filter, false, charSet );
	}
	
	/**
	 * Defaults to `UTF-8`.
	 */
	public static String[] readFiles ( String dirPath, FilenameFilter filter, boolean ignoreMissingDir ) throws UncheckedIOException {
		return readFiles ( dirPath, filter, ignoreMissingDir, UTF_8 );
	}

	/**
	 * Defaults to missingDir = false, `UTF-8`.
	 */
	public static String[] readFiles ( String dirPath, FilenameFilter filter ) throws UncheckedIOException {
		return readFiles ( dirPath, filter, false, UTF_8 );
	}

	/**
	 * Defaults to any file
	 */
	public static String[] readFiles ( String dirPath, boolean ignoreMissingDir, Charset charSet ) throws UncheckedIOException {
		return readFiles ( dirPath, null, ignoreMissingDir, charSet );
	}

	/**
	 * Defaults to any file, UTF-8
	 */
	public static String[] readFiles ( String dirPath, boolean ignoreMissingDir ) throws UncheckedIOException {
		return readFiles ( dirPath, null, ignoreMissingDir, UTF_8 );
	}

	/**
	 * Defaults to any file, ignoreMissingDir = false 
	 */
	public static String[] readFiles ( String dirPath, Charset charSet ) throws UncheckedIOException {
		return readFiles ( dirPath, null, false, charSet );
	}
	
	/**
	 * Defaults to any file, ignoreMissingDir = false, UTF-8
	 */
	public static String[] readFiles ( String dirPath ) throws UncheckedIOException {
		return readFiles ( dirPath, null, false, UTF_8 );
	}

	
	/**
	 * Returns a new stream assuming that the input comes from some I/O describing one
	 * item per line. Currently, 
	 * 
	 * <ul>
	 *   <li>if doTrim, {@link StringUtils#trimToNull(String) trims to null} and then passes the result 
	 *   downstream</li>
	 *   <li>it removes blank lines (empty and space-like only lines)</li>
	 *   <li>it removes lines starting with '#', ie, comments (spaces before are allowed)</li>
	 * </ul>
	 * 
	 * TODO: write unit tests (it's being used in KnetMiner)
	 */
	public static Stream<String> processIOItems ( Stream<String> ioItemsStrm, boolean doTrim )
	{
		Validate.notNull ( ioItemsStrm, "processIOItems(), can't process a null stream" );
		
		if ( doTrim ) ioItemsStrm = ioItemsStrm.map ( StringUtils::trimToNull );
		
		return ioItemsStrm.filter ( p -> p != null )
		.filter ( p -> !StringUtils.isWhitespace ( p ) )
		.filter ( p -> !p.trim ().startsWith ( "#" ) );
	}

	/**
	 * Defaults to ioItemsStrm = true
	 */
	public static Stream<String> processIOItems ( Stream<String> ioItemsStrm )
	{
		return processIOItems ( ioItemsStrm, true );
	}

	
	/**
	 * Facility to get a reader from a resource, uses {@link Resources#getResource(Class, String)} and
	 * {@link URL#openStream()}.
	 */
	public static Reader openResourceReader ( Class<?> clazz, String path, Charset charset ) throws IOException
	{
		return new InputStreamReader ( Resources.getResource ( clazz, path ).openStream (), charset );
	}	


	/**
	 * Defaults to "UTF-8"
	 */
	public static Reader openResourceReader ( Class<?> clazz, String path ) throws IOException
	{
		return openResourceReader ( clazz, path, UTF_8 );
	}
	
	
	public static Reader openResourceReader ( String path, Charset charset ) throws IOException
	{
		return new InputStreamReader ( Resources.getResource ( path ).openStream (), charset );
	}

	
	/**
	 * Defaults to UTF-8
	 */
	public static Reader openResourceReader ( String path ) throws IOException
	{
		return openResourceReader ( path, UTF_8 );
	}
	
	
	

	/**
	 * Facility to read a resource from the class loader associated to a class. 
	 */
	public static String readResource ( Class<?> clazz, String path, Charset charset ) throws IOException
	{
		return readResource ( clazz.getClassLoader (), path, charset );
	}


	/** 
	 * <b>WARNING</b>: after 5.0 this uses UTF-8 as default and not the system default!
	 */
	public static String readResource ( Class<?> clazz, String path ) throws IOException
	{
		return readResource ( clazz, path, UTF_8 );
	}
	
	
	/**
	 * Uses the class loader in the current thread, or the one in {@link Resources}.
	 */
	public static String readResource ( String path, Charset charset ) throws IOException
	{
		URL url = Resources.getResource ( path );
		return Resources.toString ( url, charset );
	}


	/**
	 * Defaults to UTF-8 
	 */
	public static String readResource ( String path ) throws IOException
	{
		return readResource ( path, UTF_8 );
	}
	
	
	/**
	 * Facility to read a resource from a class loader.
	 * @see ClassLoader#getResource(String)
	 * 
	 */
	public static String readResource ( ClassLoader classLoader, String path, Charset charset ) throws IOException
	{
		URL url = classLoader.getResource ( path );
		return Resources.toString ( url, charset );
	}
	
	/**
	 * Defaults to UTF-8
	 */
	public static String readResource ( ClassLoader classLoader, String path ) throws IOException
	{
		return readResource ( classLoader, path, UTF_8 );
	}

	
	/**
	 * Writes the string to the file and then closes it
	 */	
	public static void writeFile ( String path, String string, Charset charSet, boolean append ) throws IOException
	{
		try ( Writer out = new BufferedWriter ( new FileWriter ( path, charSet, append ), 1 << 20 ) )
		{
			out.write ( string );
		}
	}
	
	/**
	 * Defaults to UTF8
	 */
	public static void writeFile ( String path, String string, boolean append ) throws IOException
	{
		writeFile ( path, string, UTF_8, false );
	}
	
	/**
	 * Defaults to UTF8, append = false
	 */
	public static void writeFile ( String path, String string ) throws IOException
	{
		writeFile ( path, string, false );
	}
	
	
	/**
	 * Reads the input stream and returns an hash for it, based on the algorithm passed as parameter. algorithm 
	 * is passed to {@link MessageDigest} 
	 */
	public static String getHash ( InputStream is, String algorithm ) throws IOException, NoSuchAlgorithmException 
	{
		MessageDigest md = MessageDigest.getInstance ( algorithm );
	  byte buffer[] = new byte [ 1024 ];
	  
	  try {
	  	for ( int read = is.read ( buffer ); read != -1; read = is.read ( buffer ) )
	  		if ( read > 0 ) md.update ( buffer, 0, read );
		} 
	  finally {
			is.close();
		}
	  byte[] digest = md.digest();
	  if ( digest == null ) return null;
	  StringBuilder strDigest = new StringBuilder ();
	  for ( int i = 0; i < digest.length; i++ )
	    strDigest.append ( Integer.toString ( ( digest[i] & 0xff ) + 0x100, 16).substring ( 1 ) );
	  return strDigest.toString ();	
	}

	/**
	 * A wrapper to {@link #getHash(InputStream, String)} that opens a file.
	 */
	public static String getHash ( File f, String algorithm ) throws IOException, NoSuchAlgorithmException {
		return getHash ( new FileInputStream ( f ), algorithm );
	}

	/**
	 * Hashes a string, using {@link #getHash(InputStream, String)}.
	 */
	public static String getHash ( String string, String algorithm ) throws NoSuchAlgorithmException
	{
		try {
			var in = ReaderInputStream.builder ()
			.setReader ( new StringReader ( string ) )
			.setCharset ( "UTF-8" )
			.get ();
			
			return getHash ( in, algorithm );
		}
		catch ( IOException ex ) {
			throw new IllegalArgumentException ( "Internal error: " + ex.getMessage (), ex );
		}
	}

	/**
	 * A wrapper of {@link #getHash(InputStream, String)} that uses the MD5 algorithm.
	 */
	public static String getMD5 ( InputStream is ) throws IOException 
	{
		try {
			return getHash ( is, "MD5" );
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	} 	
	
	/**
	 * A wrapper of {@link #getHash(InputStream, File)} that uses the MD5 algorithm.
	 */
	public static String getMD5 ( File f ) throws IOException 
	{
		try {
			return getHash ( f, "MD5" );
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}

	/**
	 * A wrapper for {@link #getHash(String, String)} that uses MD5.
	 */
	public static String getMD5 ( String string )
	{
		try {
			return getHash ( string, "MD5" );
		}
		catch ( NoSuchAlgorithmException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
	}
	
	
	/**
	 * Creates a URI and intercepts URISyntaxException, throwing IllegalArgumentException if such an exception
	 * occurs. I've created this method because it's often very annoying to have a checked exception here.
	 */
	public static URI uri ( String uriStr ) 
	{
		try {
			return new URI ( uriStr );
		}
		catch ( URISyntaxException ex ) {
			throw buildEx ( IllegalArgumentException.class, ex, 
				"Internal error with URI \"%s\": $cause", uriStr
			);
		}
	}

	/**
	 * @see #uri(String)
	 */
	public static URL url ( String urlStr ) 
	{
		try {
			return new URL ( urlStr );
		}
		catch ( MalformedURLException ex ) {
			throw buildEx ( IllegalArgumentException.class, ex, 
				"Internal error with URL \"%s\": $cause", urlStr
			);
		}
	}
	
}
