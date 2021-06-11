package uk.ac.ebi.utils.collections;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

/**
 * A variant of the map interface that has the purpose of easing the manipulation of
 * options based on string values. These are typically fetched from {@link Properties}
 * and the like.
 * 
 * See unit tests for details.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Sep 2020</dd></dl>
 *
 */
public interface OptionsMap extends Map<String, Object>
{
	/**
	 * Convert a string value returned by the key using the converter parameter.
	 * 
	 * If the key is null, returns the default
	 * If it's a string, returns the result of the conversion
	 * Else, casts it to V, possibly raising {@link ClassCastException}
	 * 
	 * The idea is that we're dealing with  a mix of object values or string values that might represent some other types.
	 * This is a base for other specific methods (eg, {@link #getDouble(String, Double)}.	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public default <V> V getOpt ( String key, V defaultValue, Function<String, V> converter ) 
	{
		Object v = this.get ( key );
		if ( v == null ) return defaultValue;
		if ( v instanceof String && converter != null ) return converter.apply ( (String) v );
		return (V) v;
	}
	
	/** Default is null */
	public default <V> V getOpt ( String key, Function<String, V> converter ) 
	{
		return getOpt ( key, null, converter );
	}
	
	/** No conversion, returned value type depends on what it was stored */
	public default <V> V getOpt ( String key, V defaultValue ) {
		return getOpt ( key, defaultValue, null );
	}

	/** null as default value and no conversion */
	public default <V> V getOpt ( String key ) {
		return getOpt ( key, null, null );
	}	
	
	
	public default Double getDouble ( String key, Double defaultValue ) 
	{
		return getOpt ( key, defaultValue, Double::parseDouble );
	}

	public default Double getDouble ( String key ) 
	{
		return getDouble ( key, null );
	}
	
	
	public default Integer getInt ( String key, Integer defaultValue ) 
	{
		return getOpt ( key, defaultValue, Integer::parseInt );
	}

	public default Integer getInt ( String key ) 
	{
		return getInt ( key, null );
	}
	
	
	public default Long getLong ( String key, Long defaultValue ) 
	{
		return getOpt ( key, defaultValue, Long::parseLong );
	}

	public default Long getLong ( String key ) 
	{
		return getLong ( key, null );
	}

	/**
	 * Expects the key to return a string and returns it as-is. In other words, this is 
	 * just to make the type casting easier. 
	 */
	public default String getString ( String key, String defaultValue ) 
	{
		return getOpt ( key, defaultValue, null );
	}

	public default String getString ( String key ) 
	{
		return getString ( key, null );
	}
	
	
	public default Boolean getBoolean ( String key, Boolean defaultValue ) 
	{
		return getOpt ( key, defaultValue, Boolean::parseBoolean );
	}

	public default Boolean getBoolean ( String key ) 
	{
		return getBoolean ( key, null );
	}
	
	/**
	 * This is based on {@link OptionsMapWrapper}.
	 */
	public static OptionsMap from ( Map<String, Object> base )
	{
		return new OptionsMapWrapper ( base );
	}

  /**
   * This is based on {@link OptionsMapWrapper}
   */
	public static OptionsMap from ( Properties base )
	{
		return new OptionsMapWrapper ( base );
	}
	
	/**
	 * This is based on {@link OptionsMapWrapper}.
	 */
	public static OptionsMap create () {
		return new OptionsMapWrapper ();
	}

	/**
	 * This is simply the chaining of {@link #from(Map)} and {@link Collections#unmodifiableMap(Map)}, ie, returns
	 * a read-only options map.
	 * 
	 */
	public static OptionsMap unmodifiableOptionsMap ( Map<String, Object> base )
	{
		return from ( Collections.unmodifiableMap ( base ) );
	}

	public static OptionsMap unmodifiableOptionsMap ( Properties base )
	{
		return unmodifiableOptionsMap ( new OptionsMapWrapper ( base ) );
	}

}
