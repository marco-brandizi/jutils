package uk.ac.ebi.utils.opt.config;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.env.StandardEnvironment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.opt.io.IOUtils;

/**
 * A simple YAML document/file loader, which is focused on configuration needs.
 * 
 * The basic function of this component is loading YAML files and mapping them to POJO classes.
 * 
 * It also offers features that are often useful when dealing with application configurations:
 * 
 * <ul>
 *   <li>It allows for inclusions, using the {@link #INCLUDES_PROP} at the YAML document/file root level (ie, not 
 *   in a nested level</li>
 *   <li>When including a file from a parent, you can extend (instead of override) fields that the parent defined 
 *   as arrays. Append {@link #MERGE_SUFFIX} to the field and it's values will be added to the field with the same
 *   name (minus the postfis). For instance, if a parent file has: {@code options: [default]} and an included file has
 *   {@code options @merge: [advanced]}, the result for options will be [default, advanced]. When mapping to JavaBean
 *   properties of type collection, the collection is populated with the merge result (and properties like order or
 *   repeatitions depdend on the exact collection type used for the JavaBean).</li>
 *   <li>Property interpolation: When using <tt>${propName}</tt> in YAML values (not field names), the referred
 *   property replaced with values in the {@link System#getProperties() JVM properties} or
 *   {@link System#getenv() environment variables}. This is based on {@link StandardEnvironment Spring}</li>
 * </ul>
 * 
 * See the unit tests for examples of use.<br/><br/>
 *
 * TODO: support for URLs<br/>
 * TODO: Support Spring SpEL for interpolation.<br/>
 * TODO: support for charsets?<br/><br/>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>14 Jun 2022</dd></dl>
 *
 */
public class YAMLUtils
{
	private static final StandardEnvironment SPRING_INTERPOLATOR = new StandardEnvironment ();
	public static final String INCLUDES_PROP = "@includes";
	public static final String MERGE_SUFFIX = "@merge";

	/**
	 * @See {@link #loadYAMLFromString(String, Class)}.
	 * 
	 */
	public static <T> T loadYAMLFromFile ( String filePath, Class<T> targetClass ) throws UncheckedIOException
	{
		return loadYAMLFromString ( IOUtils.readFile ( filePath ), targetClass, filePath );
	}

	/**
	 * Maps a YAML file to a target, after it has been processes as explained above.
	 */
	public static <T> T loadYAMLFromString ( String yamlStr, Class<T> targetClass )
	{
		return loadYAMLFromString ( yamlStr, targetClass, "" );
	}
	
	/**
	 * 
	 * Maps the result of {@link #rawLoadingFromString(String, Map, String) low-level processing} to the 
	 * target. This is wrapped by similar public methods, we don't think it's useful to expose
	 * filePath here.
	 *  
	 * @param filePath is used for the relative paths mentioned by the {@link #INCLUDES_PROP includes directive}.
	 * It can be null if such directive isn't used, or it's used with absolute paths only. 
	 * 
	 */
	private static <T> T loadYAMLFromString ( String yamlStr, Class<T> targetClass, String filePath ) 
		throws UncheckedIOException
	{
		Map<String, Object> yamlo = rawLoadingFromString ( yamlStr, new HashMap<> (), filePath );
		var mapper = new ObjectMapper ();
		var result = mapper.convertValue ( yamlo, targetClass );
		
		return result;
	}
	
	
	private static Map<String, Object> rawLoadingFromFile ( String filePath, Map<String, Object> resultJso )
		throws UncheckedIOException
	{
		return rawLoadingFromString ( IOUtils.readFile ( filePath ), resultJso, filePath );
	}
	
	/**
	 * Process the YAML recursively. That is, {@link #mergeJsObjects(Map, Map) merges reccursively} the current YAML to 
	 * resultJso, and then calls itself (indirectly, via {@link #rawLoadingFromFile(String, Map)}) for each 
	 * {@link #INCLUDES_PROP includes}.
	 *   
	 */
	@SuppressWarnings ( "unchecked" )
	private static Map<String, Object> rawLoadingFromString ( 
		String yamlStr, Map<String, Object> resultJso, String filePath ) throws UncheckedIOException
	{
		// Interpolate ${variable}, can contain system properties or environment properties
		// TODO: SpEL
		yamlStr = SPRING_INTERPOLATOR.resolvePlaceholders ( yamlStr );
		
		var mapper = new ObjectMapper ( new YAMLFactory () );
		Map<String, Object> jso;
		try {
			jso = mapper.readValue ( yamlStr, HashMap.class );
		}
		catch ( JsonProcessingException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedIOException.class, ex, 
				"Error while processing the YAML file: \"%s\": $cause", Optional.ofNullable ( filePath ).orElse ( "<N.A.>" )
			);
		}
		
		// Consider the merge directive recursively
		mergeJsObjects ( resultJso, jso );
		
		
		// Now that the current doc (ie, jso) has been merged to resultJso, including the merge directive's logic
		// see if there are includes that recursively require the same 
		//

		Object includesObj = jso.get ( INCLUDES_PROP );
		if ( includesObj == null ) return resultJso;
		
		if ( ! ( includesObj instanceof Collection ) ) ExceptionUtils.throwEx (
			IllegalArgumentException.class,
			"Error while loading YAML file \"%s\": %s directive must contain an array",
			Optional.ofNullable ( filePath ).orElse ( "<N.A.>" ),
			INCLUDES_PROP
		);
				
		// Deal with relative paths
		// null is a rare event here (filePath should be a file), but SpotBugs recommended to check it
		//
		String basePath = filePath == null 
			? null 
			: Optional.ofNullable ( Path.of ( filePath ).toAbsolutePath ().getParent () )
				.map ( Object::toString )
				.orElseThrow ( () -> ExceptionUtils.buildEx ( 
					IllegalArgumentException.class, "Invalid file path: %s", filePath )
		);

		for ( String include: (Collection<String>) includesObj )
		{
			if ( !( basePath == null || include.startsWith ( "/" ) ) )
				include = basePath + "/" + include;
			rawLoadingFromFile ( include, resultJso );
		}
		
		return resultJso;
	}
	
	/**
	 * Merges a JSON object into a parent, used for nested merges within included
	 * YAML (jso) and including one (parentJso). 
	 * 
	 * Note that includes are managed separately from this.
	 */
	@SuppressWarnings ( "unchecked" )
	private static void mergeJsObjects ( Map<String, Object> parentJso, Map<String, Object> jso )
	{
		for ( String key: jso.keySet () )
		{
			Object val = jso.get ( key );
			
			// Not my duty
			if ( INCLUDES_PROP.equals ( key ) ) continue;
			else if ( key.matches ( ".+" + MERGE_SUFFIX + "\\s*$" ) )
			{
				// Merges lists into lists, or structured objects into structured objects
				//
				
				// First things first
				String actualKey = key.replaceFirst ( "\\s*" + MERGE_SUFFIX + "\\s*$", "" );
								
				// Collections, just merge to the parent, if it's a collection too 
				if ( val instanceof Collection )
				{
					Object parentVal = parentJso.computeIfAbsent ( actualKey, k -> new ArrayList<> () ); 

					if ( !( parentVal instanceof Collection ) )
						ExceptionUtils.throwEx ( IllegalArgumentException.class,
							"YAML error: %s directive used with an array-like field '%s', but parent value isn't an array", 
							MERGE_SUFFIX, actualKey
					);
					

					((Collection<Object>) parentVal).addAll ( (Collection<Object>) val ); 
				}
				// Maps, dive down with recursion over the parent and current value
				else if ( val instanceof Map )
				{
					Object parentVal = parentJso.computeIfAbsent ( actualKey, k -> new HashMap<String, Object> () ); 
					
					if ( !( parentVal instanceof Map ) )
						ExceptionUtils.throwEx ( IllegalArgumentException.class,
							"YAML error: %s directive used with a map field '%s', but parent value isn't a map", 
							MERGE_SUFFIX, actualKey
					);

					mergeJsObjects ( (Map<String, Object>) parentVal, (Map<String, Object>) val );
				}
				else
					// Merge asked, but it's not a mergeable type, meh!
					ExceptionUtils.throwEx ( IllegalArgumentException.class,
						"YAML error: %s directive used for the field %s, but the parent has a single plain value (must be object or array)", 
						MERGE_SUFFIX, actualKey
				);
			}
			else
				// No merge directive, just override
				parentJso.put ( key, val );
		} // for key
	}
}
