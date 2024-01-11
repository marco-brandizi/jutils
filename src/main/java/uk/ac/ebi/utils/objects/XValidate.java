package uk.ac.ebi.utils.objects;

import java.util.Collection;

import org.apache.commons.lang3.Validate;

/**
 * Logical extension to {@link Validate}, which adds more methods. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>26 Oct 2023</dd></dl>
 *
 */
public class XValidate
{
	/**
	 * Checks that the collection isn't null and isn't empty 
	 */
  public static <C extends Collection<?>> C notEmpty (final C collection, final String message, final Object... values) 
  {
    Validate.isTrue ( collection != null && !collection.isEmpty (), message, values );
    return collection;
  }
}
