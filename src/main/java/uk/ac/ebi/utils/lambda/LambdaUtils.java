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
	public static Callable<Void> toCallable ( Runnable runnable )
	{
		return () -> { runnable.run (); return null; };
	}
	
	public static <T> Supplier<T> toSupplier ( Callable<T> callable )
	{
		return Exceptions.sneak ().supplier ( () -> callable.call () );
	}

	public static Supplier<Void> toSupplier ( Runnable runnable )
	{
		return toSupplier ( toCallable ( runnable ) );
	}
	
}
