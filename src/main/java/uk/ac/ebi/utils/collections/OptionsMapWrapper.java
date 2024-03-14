package uk.ac.ebi.utils.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An implementation of {@link OptionsMap} that is simply based on the delegator pattern 
 * and forwards all its operations to an underlining {@link Map}.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Sep 2020</dd></dl>
 *
 */
public class OptionsMapWrapper implements OptionsMap
{
	private final Map<String, Object> base;

	/**
	 * Creates an options map with an empty {@link HashMap} as its base.
	 */
	public OptionsMapWrapper ()
	{
		this ( new HashMap<> () );
	}

	public OptionsMapWrapper ( Map<String, Object> base )
	{
		this.base = base;
	}
		
	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	public OptionsMapWrapper ( Properties base )
	{
		// This works fine as long as the base is used properly, ie, to store string->string
		this ( (Map) base );
	}
	
	public int size ()
	{
		return base.size ();
	}

	public boolean isEmpty ()
	{
		return base.isEmpty ();
	}

	public boolean containsKey ( Object key )
	{
		return base.containsKey ( key );
	}

	public boolean containsValue ( Object value )
	{
		return base.containsValue ( value );
	}

	public Object get ( Object key )
	{
		return base.get ( key );
	}

	public Object put ( String key, Object value )
	{
		return base.put ( key, value );
	}

	public Object remove ( Object key )
	{
		return base.remove ( key );
	}

	public void putAll ( Map<? extends String, ? extends Object> m )
	{
		base.putAll ( m );
	}

	public void clear ()
	{
		base.clear ();
	}

	public Set<String> keySet ()
	{
		return base.keySet ();
	}

	public Collection<Object> values ()
	{
		return base.values ();
	}

	public Set<Entry<String, Object>> entrySet ()
	{
		return base.entrySet ();
	}

	public boolean equals ( Object o )
	{
		return base.equals ( o );
	}

	public int hashCode ()
	{
		return base.hashCode ();
	}

	public Object getOrDefault ( Object key, Object defaultValue )
	{
		return base.getOrDefault ( key, defaultValue );
	}

	public void forEach ( BiConsumer<? super String, ? super Object> action )
	{
		base.forEach ( action );
	}

	public void replaceAll ( BiFunction<? super String, ? super Object, ? extends Object> function )
	{
		base.replaceAll ( function );
	}

	public Object putIfAbsent ( String key, Object value )
	{
		return base.putIfAbsent ( key, value );
	}

	public boolean remove ( Object key, Object value )
	{
		return base.remove ( key, value );
	}

	public boolean replace ( String key, Object oldValue, Object newValue )
	{
		return base.replace ( key, oldValue, newValue );
	}

	public Object replace ( String key, Object value )
	{
		return base.replace ( key, value );
	}

	public Object computeIfAbsent ( String key, Function<? super String, ? extends Object> mappingFunction )
	{
		return base.computeIfAbsent ( key, mappingFunction );
	}

	public Object computeIfPresent ( String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction )
	{
		return base.computeIfPresent ( key, remappingFunction );
	}

	public Object compute ( String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction )
	{
		return base.compute ( key, remappingFunction );
	}

	public Object merge ( String key, Object value, BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction )
	{
		return base.merge ( key, value, remappingFunction );
	}

	@Override
	public String toString ()
	{
		return "[" + this.getClass ().getSimpleName () + "] " + base.toString ();
	}
	
}
