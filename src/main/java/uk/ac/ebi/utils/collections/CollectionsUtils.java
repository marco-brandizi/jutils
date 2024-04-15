package uk.ac.ebi.utils.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Collections-related utils.
 * 
 * TODO: tests.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Aug 2023</dd></dl>
 *
 */
public class CollectionsUtils
{
	/**
	 * Converts the value into an immutable {@link Collection}. 
	 * 
	 * If the value is null, returns an {@link Collections#emptySet() empty set}.
	 * If the value isn't a collection, returns a {@link Collections#singleton(Object) singleton set}.
	 * If the value is already a collection, returns its 
	 * {@link Collections#unmodifiableCollection(Collection) unmodifiable wrapper}.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Collection<T> asCollection ( Object value )
	{
		if ( value == null ) return Collections.emptySet ();
		
		if ( value instanceof Collection ) 
			return Collections.unmodifiableCollection ( (Collection<T>) value );
		
		return Collections.singleton ( (T) value );
	}
	
	/**
	 * Uses {@link #asCollection(Object)} to return an unmodifiable list out of the value.
	 * 
	 * If the result from {@link #asCollection(Object)} is a list, returns it, else 
	 * creates a list from such result and returns an unmodifiable wrapper of it.
	 */
	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	public static <T> List<T> asList ( Object value )
	{
		var result = asCollection ( value );
		if ( result instanceof List ) return (List<T>) result;
		
		return Collections.unmodifiableList ( new ArrayList ( result ) );
	}
	

	/**
	 * Uses {@link #asCollection(Object)} to return an unmodifiable set out of the value.
	 * 
	 * If the result from {@link #asCollection(Object)} is a set, returns it, else 
	 * creates a set from such result and returns an unmodifiable wrapper of it.
	 */
	@SuppressWarnings ( { "unchecked", "rawtypes" } )
	public static <T> Set<T> asSet ( Object value )
	{
		var result = asCollection ( value );
		if ( result instanceof Set ) return (Set<T>) result;
		
		return Collections.unmodifiableSet ( new HashSet ( result ) );
	}
	
	
	/**
	 * Converts a possibly-collection value into a singleton.
	 * 
	 * If the value is null, returns null.
	 * If the value isn't a collection, returns the value unabridged.
	 * 
	 * If the value is a collection with one element only, returns the element
	 * in the collection. 
	 * 
	 * If such collection contains more than one element, 
	 *   if failIfMany is true, throws {@link IllegalArgumentException}
	 *   if failIfMany is false, returns the first element in the collection value, which is then 
	 *   undetermined
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> T asValue ( Object value, boolean failIfMany )
	{
		if ( value == null ) return null;
		
		if ( ! ( value instanceof Collection ) ) return (T) value;
			
		// Deal with a collection
		var coll = (Collection<T>) value;
		
		if ( coll.isEmpty () ) return null;
		
		Iterator<T> itr = coll.iterator ();
		var result = itr.next ();
		
		if ( failIfMany && itr.hasNext () ) throw new IllegalArgumentException (
			"Attempt to extract a singleton from a multi-value collection"
		);
		
		return result;
	}
	
	/**
	 * Wrapper with failIfMany = false
	 */
	public static <T> T asValue ( Object value )
	{
		return asValue ( value, false );
	}
	
	
	/**
	 * TODO: newXXX() and unmodifiableXXX(), comment and test 
	 */
	
	public static <T, C extends Collection<T>> C newCollection ( Collection<? extends T> coll, Supplier<C> provider )
	{
		C result = provider.get ();
		if ( coll != null && !coll.isEmpty () ) result.addAll ( coll );
		return result;
	}
	
	public static <T> Set<T> newSet ( Collection<? extends T> coll )
	{
		return newCollection ( coll, HashSet::new );
	}

	public static <T> List<T> newList ( Collection<? extends T> coll )
	{
		return newCollection ( coll, ArrayList::new );
	}

	public static <K,V> Map<K,V> newMap ( Map<? extends K, ? extends V> map, Supplier<Map<K, V>> provider )
	{
		Map<K,V> result = provider.get ();
		if ( map != null && !map.isEmpty () ) result.putAll ( map );
		return result;
	}

	public static <K,V> Map<K,V> newMap ( Map<? extends K, ? extends V> map )
	{
		return newMap ( map, HashMap::new );
	}
	
	
	public static <T, C extends Collection<T>, CP extends Collection<? extends T>> 
	C unmodifiableCollection ( 
		CP coll,
		Function<CP, C> wrapper,
		Supplier<C> emptyProvider
	)
	{
		if ( coll == null || coll.isEmpty () ) return emptyProvider.get ();
		return wrapper.apply ( coll );
	}
	
	public static <T> Set<T> unmodifiableSet ( Set<? extends T> set )
	{
		return unmodifiableCollection ( set, Collections::unmodifiableSet, Set::of );
	}

	public static <T> List<T> unmodifiableList ( List<? extends T> list )
	{
		return unmodifiableCollection ( list, Collections::unmodifiableList, List::of );
	}
	
	public static <K, V> Map<K, V> unmodifiableMap ( 
		Map<? extends K, ? extends V> map,
		Supplier<Map<K, V>> emptyProvider
	)
	{
		if ( map == null || map.isEmpty () ) return emptyProvider.get ();
		return Collections.unmodifiableMap ( map );
	}

	public static <K, V> Map<K, V> unmodifiableMap ( Map<? extends K, ? extends V> map )
	{
		return unmodifiableMap ( map, Map::of );
	}
}
