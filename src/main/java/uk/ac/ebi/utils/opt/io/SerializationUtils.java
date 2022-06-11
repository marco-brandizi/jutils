package uk.ac.ebi.utils.opt.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.exceptions.UncheckedFileNotFoundException;

/**
 * Object serialisation/unserialisation utils that work with IO streams.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Aug 2020</dd></dl>
 *
 */
public class SerializationUtils
{
	/**
	 * Just like {@link ObjectOutputStream#writeObject(Object)}, with the checked exception 
	 * wrapper included
	 *
	 * @throws UncheckedIOException
	 * @throws NullPointerException if the object is null
	 */
	public static void serialize ( ObjectOutputStream out, Object object )
	{
		if ( object == null ) throw new NullPointerException ( "Cannot serialize a null object" );
		try
		{
			out.writeObject ( object );
		}
		catch ( IOException ex )
		{
			ExceptionUtils.throwEx ( UncheckedIOException.class, ex,
				"Error while serialising instance of %s: %s", 
				object.getClass ().getSimpleName (),
				ex.getMessage ()
			);
		}
	}
	
	/**
	 * Wraps the opening of a buffered {@link FileOutputStream} and {@link ObjectOutputStream}, then
	 * then {@link ObjectOutputStream#writeObject(Object) writes} the object in it and closes.
	 *
	 * @throws UncheckedFileNotFoundException, UncheckedIOException
	 */
	public static void serialize ( File outf, Object object, boolean append )
	{
		BiConsumer<Class<? extends RuntimeException>, Exception> thrower = (extype, cause) ->
			ExceptionUtils.throwEx ( extype, cause,
				"Error while serialising instance of %s to file '%s': %s", 
				object == null ? "<null>" : object.getClass ().getSimpleName (),
				outf.getAbsolutePath (),
				cause.getMessage ()
			);
		
		try ( ObjectOutputStream out = new ObjectOutputStream ( 
			new BufferedOutputStream ( new FileOutputStream ( outf, append ), 1 << 20 )
		))
		{
			serialize ( out, object );
		}
		catch ( FileNotFoundException ex ) {
			thrower.accept ( UncheckedFileNotFoundException.class, ex );
		}
		catch ( Exception ex ) {
			thrower.accept ( UncheckedIOException.class, ex );
		}
	}
	
	/**
	 * Defaults to false (creates the file from scratch)
	 */
	public static void serialize ( File outf, Object object )
	{
		serialize ( outf, object, false );
	}
	
	public static void serialize ( String outPath, Object object, boolean append )
	{
		serialize ( new File ( outPath ), object, append );
	}

	public static void serialize ( String outPath, Object object )
	{
		serialize ( outPath, object, false );
	}
	
	
	/**
	 * Just like {@link ObjectInputStream#readObject()}, with the checked exception 
	 * wrapper included
	 *
	 * @throws UncheckedIOException
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> T deserialize ( ObjectInputStream in )
	{
		try
		{
			return (T) in.readObject ();
		}
		catch ( IOException | ClassNotFoundException ex )
		{
			throw ExceptionUtils.buildEx ( UncheckedIOException.class, ex,
				"Error while deserialising an object: %s", 
				ex.getMessage ()
			);
		}
	}
	
	/**
	 * Wraps the opening of a buffered {@link FileOutputStream} and {@link ObjectOutputStream}, then
	 * then {@link ObjectOutputStream#writeObject(Object) writes} the object in it and closes.
	 *
	 * @throws UncheckedFileNotFoundException, UncheckedIOException
	 */
	public static <T> T deserialize ( File inf )
	{
		BiFunction<Class<? extends RuntimeException>, Exception, RuntimeException> thrower = 
		(extype, cause) -> ExceptionUtils.buildEx ( extype, cause,
			"Error while deserialising object from file '%s': %s",			
			inf.getAbsolutePath (),
			cause.getMessage ()
		);
		
		try ( ObjectInputStream in = new ObjectInputStream ( 
			new BufferedInputStream ( new FileInputStream ( inf ), 1 << 20 )
		))
		{
			return deserialize ( in );
		}
		catch ( FileNotFoundException ex ) {
			throw thrower.apply ( UncheckedFileNotFoundException.class, ex );
		}
		catch ( Exception ex ) {
			throw thrower.apply ( UncheckedIOException.class, ex );
		}
	}
	
	public static <T> T deserialize ( String inpath )
	{
		return deserialize ( new File ( inpath ) );
	}
}
