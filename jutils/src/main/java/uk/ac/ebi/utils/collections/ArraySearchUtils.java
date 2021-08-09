package uk.ac.ebi.utils.collections;

import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Utilities about searches over arrays.
 *
 * <dl><dt>date</dt><dd>25 Sep 2013</dd></dl>
 * @author Marco Brandizi
 *
 */
public class ArraySearchUtils
{
	private ArraySearchUtils () {}
	
	/**
	 * Tells if a value is equals to one of the values specified in an array.
	 *  
	 * @param value the value to check, if it is null, the method returns null in case comparator.compare ( null, null ) 
	 *   returns 0 and testValues contains null.
	 * @param comparator the comparator to be used for equality test. Note that what this returns when two values are different
	 *   isn't relevant here. {@link #isOneOf(Object, Object...)} and {@link #isOneOfByIdentity(Object, Object...)} are
	 *   convenient variants for common use cases.
	 * @param testValues the values that the target parameter has to be compared to
	 * 
	 */
	@SafeVarargs
	public static <T extends Comparable<? super T>> boolean isOneOf ( T value, Comparator<T> comparator, T... testValues )
	{
		if ( testValues == null ) return false;
		for ( T testValue: testValues ) 
			if ( comparator.compare ( value, testValue ) == 0 ) return true;
		return false;
	}
	
	/**
	 * A variant of {@link #isOneOf(Object, Comparator, Object...)} that uses the natural comparison as comparator (i.e.
	 * T.equals().
	 */
	@SafeVarargs
	public static <T extends Comparable<? super T>> boolean isOneOf ( T value, T... testValues )
	{
		return isOneOf ( value, (T o1, T o2) -> ObjectUtils.compare ( o1, o2 ), testValues );
	}

	/**
	 * A variant of {@link #isOneOf(Object, Comparator, Object...)} that uses the identity comparison criterion (i.e., 
	 * the '==' operator).
	 * 
	 */
	public static <T extends Comparable<? super T>> boolean isOneOfByIdentity ( T value, T... testValues )
	{
		return isOneOf ( 
			value, 
			(o1, o2) -> o1 == o2 ? 0 : ObjectUtils.compare ( o1, o2 ),
			testValues 
		);
	}

}
