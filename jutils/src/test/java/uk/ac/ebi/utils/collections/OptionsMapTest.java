package uk.ac.ebi.utils.collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Functions;

/**
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Sep 2020</dd></dl>
 *
 */
public class OptionsMapTest
{	
	private static final Map<String, Object> REF_OPTS = Map.of (
		"opt.str", "Value A",
		"opt.int", 42,
		"opt.dbl", 10.5,
		"opt.obj", new MutableBoolean ( true )
	);
	
	// The new map is just because Map.of() created a read-only map and 
	// we have tests changing it. Normally you have a modifiable map 
	private OptionsMap opts = OptionsMap.from ( new HashMap<> ( REF_OPTS ) );
	
	@Test
	public void testBasics ()
	{
		assertEquals ( "Wrong size for OptionsMap!", REF_OPTS.size (), opts.size () );
		assertTrue ( "Wrong opt.a!", opts.get ( "opt.str" ) instanceof String );
		assertEquals ( "Wrong opt.int!", REF_OPTS.get ( "opt.int" ), opts.get ( "opt.int" ) );
	}

	@Test
	public void testGetNum ()
	{
		assertEquals ( "Wrong opt.int!", (Integer) REF_OPTS.get ( "opt.int" ), opts.getInt ( "opt.int" ) );
		assertEquals ( "Wrong opt.dbl!", (Double) REF_OPTS.get ( "opt.dbl" ), opts.getDouble ( "opt.dbl" ) );
	}
	
	@Test
	public void testGetDefaults ()
	{
		assertEquals ( "Wrong opt.int with default!", (Integer) REF_OPTS.get ( "opt.int" ), opts.getInt ( "opt.int", -1 ) );
		assertEquals ( "Wrong default!", -1, (int) opts.getInt ( "opt.foo", -1 ) );
	}

	@Test
	public void testGetString ()
	{
		assertEquals ( "Wrong opt.str", (String) REF_OPTS.get ( "opt.str" ), opts.getString ( "opt.str" ) );
	}

	@Test ( expected = ClassCastException.class )
	public void testFailOnWrongType ()
	{
		opts.getString ( "opt.obj" );
	}
	
	@Test
	public void testCustomConverters ()
	{
		// as-is conversion with facilitated type cast.
		MutableBoolean obj = opts.getOpt ( "opt.obj" );
		assertEquals ( "Wrong opt.obj as-is", (MutableBoolean) REF_OPTS.get ( "opt.obj" ), obj );

		// Our own conversion
		int [] v = new int [] { 1, 2 };
		opts.put ( "opt.pair", v [ 0 ] + "," + v [ 1 ] );
		Function<String, int[]> converter = s -> {
			String[] sa = s.split ( "," );
			return new int [] { Integer.parseInt ( sa [ 0 ] ), Integer.parseInt ( sa [ 1 ] ) }; 
		};
		assertTrue ( "Wrong custom conversion", Arrays.equals ( v, opts.getOpt ( "opt.pair", converter ) ) );

		// default
		final var defaultVal = "foo value";
		assertEquals ( "Wrong opt.obj conversion", defaultVal, opts.getOpt ( "opt.foo", defaultVal, Functions.identity () ) );
	}
	
	@Test
	public void testCreateEmpty ()
	{
		var opts = OptionsMap.create ();
		assertTrue ( "Empty options not empty!", opts.isEmpty () );

		String fooName = "fooOpt", fooValue = "fooValue";
		
		opts.put ( fooName, fooValue );
		assertEquals ( "Entry not added!", 1, opts.size () );
		assertEquals ( "Wrong option retrieved!", fooValue, opts.get ( fooName ) );
	}
}
