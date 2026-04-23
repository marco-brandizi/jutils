package uk.ac.ebi.utils.lambda;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.machinezoo.noexception.Exceptions;


/**
 * Utilities for functional programming and lambda syntax.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jan 2021</dd></dl>
 *
 */
public class LambdaUtils
{
	/**
	 * Turns a {@link Runnable} into a {@link Callable} that returns null. Useful as an adapter. 
	 */
	public static Callable<Void> toCallable ( Runnable runnable )
	{
		return () -> { runnable.run (); return null; };
	}
	
	/**
	 * Turns a {@link Callable} into a {@link Supplier}, including a checked exception wrapper.
	 */
	public static <T> Supplier<T> toSupplier ( Callable<T> callable )
	{
		return Exceptions.sneak ().supplier ( () -> callable.call () );
	}

	/**
	 * Turns a {@link Runnable} into a {@link Supplier} that returns null. 
	 * 
	 */
	public static Supplier<Void> toSupplier ( Runnable runnable )
	{
		return () -> { runnable.run (); return null; };
	}
	
}
