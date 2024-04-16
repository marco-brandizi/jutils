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
	 * Utility for as$Collection( value ) methods.
	 * 
	 * @return a collection containing the value parameter. If value is null, 
	 * an empty collection, if it's already of type C/collClass, return the 
	 * value itself, if it's another collection, return an instance of C
	 * with the values copied into it. 
	 * 
	 * @param value
	 * @param collClass needed to know if value is an instance of C
	 * @param collCreator how to create a new empty collection when value is a collection, but
	 * not an instance of C/collClass. If this is null, returns value in this case (WARN: it must 
	 * be compatible).
	 * @param emptyCollCreator creates and empty collection when value is null
	 * @param singletonCreator creates a unmodifiable singleton of type C when value is not a collection
	 * @param unmodifiableWrapper used to wrap the result into an unmodifiable collection of type C
	 */
	@SuppressWarnings ( "unchecked" )
	private static <T, C extends Collection<T>> C asCollection ( 
		Object value,
		Class<C> collClass,
		Supplier<C> collCreator,
		Supplier<C> emptyCollCreator,
		Function<Object, C> singletonCreator,
		UnaryOperator<C> unmodifiableWrapper
	)
	{
		if ( value == null ) return emptyCollCreator.get ();
		
		C result;
		if ( collClass.isInstance ( value ) )
			result = (C) value;
		else if ( value instanceof Collection coll )
		{
			if ( collCreator == null )
				result = (C) coll;
			else {
				result = collCreator.get ();
				result.addAll ( coll );
			}
		}
		else
			return singletonCreator.apply ( value );
		
		return unmodifiableWrapper.apply ( result );
	}
	
	
	/**
	 * Converts the value into an immutable {@link Collection}. 
	 * 
	 * If the value is null, returns an {@link Collections#emptySet() empty set}.
	 * If the value isn't a collection, returns a {@link Collections#singleton(Object) singleton set}.
	 * If the value is already a collection, returns its 
	 * {@link Collections#unmodifiableCollection(Collection) unmodifiable wrapper}.
	 * 
	 * Note that, in the latter case, it <b>doesn't copy</b> the original collection.
	 * 
	 * This is based on {@link #asCollection(Object, Class, Supplier, Supplier, Function, UnaryOperator)}.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> Collection<T> asCollection ( Object value )
	{
		return asCollection ( value,
			Collection.class, // collection class
			null, // collCreator
			Collections::emptySet, // emptyCollCreator
			Collections::singleton, // singletonCreator
			Collections::unmodifiableCollection // unmodifiableWrapper
		);
	}
	
	/**
	 * Similar to {@link #asCollection(Object)}, returns an unmodifiable 
	 * list out of the value.
	 * 
	 * This is based on {@link #asCollection(Object, Class, Supplier, Supplier, Function, UnaryOperator)}.
	 */
	@SuppressWarnings ( { "unchecked" } )
	public static <T> List<T> asList ( Object value )
	{
		return asCollection (
			value,
			List.class, // coll class
			ArrayList::new, // coll creator
			List::of, // empty coll creator
			List::of, // singleton creator
			Collections::unmodifiableList // unmodifiable wrapper
		);
	}
	

	/**
	 * Similar to {@link #asCollection(Object)}, returns an unmodifiable set out of the value.
	 * if the value is a collection, uses it to create a new set copy.
	 * 
	 * This is based on {@link #asCollection(Object, Class, Supplier, Supplier, Function, UnaryOperator)}.
	 */
	@SuppressWarnings ( { "unchecked" } )
	public static <T> Set<T> asSet ( Object value )
	{
		return asCollection (
			value,
			Set.class, // coll class
			HashSet::new, // coll creator
			Set::of, // empty coll creator
			Set::of, // singleton creator
			Collections::unmodifiableSet // unmodifiable wrapper
		);
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
	 *   
	 * TODO: consider other iterables.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> T asValue ( Object value, boolean failIfMany )
	{
		if ( value == null ) return null;
		if ( !( value instanceof Collection ) ) return (T) value;
			
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
	
	
	public static <T, C extends Collection<T>> C 
	  newCollectionIfNull ( C coll, Supplier<C> provider )
	{
		if ( coll != null ) return coll;
		return provider.get ();
	}
	
	public static <T> Set<T> newSetIfNull ( Set<T> set )
	{
		return newCollectionIfNull ( set, HashSet::new );
	}
	
	public static <T> List<T> newListIfNull ( List<T> list )
	{
		return newCollectionIfNull ( list, ArrayList::new );
	}
	
	public static <K, V, M extends Map<K,V>> M newMapIfNull ( M map, Supplier<M> provider )
	{
		if ( map != null && !map.isEmpty () ) return map;
		return provider.get ();
	}
	
	public static <K, V> Map<K, V> newMapIfNull ( Map<K, V> map )
	{
		return newMapIfNull ( map, HashMap::new );
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
