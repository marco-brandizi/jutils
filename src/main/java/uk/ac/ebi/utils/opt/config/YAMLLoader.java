package uk.ac.ebi.utils.opt.config;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
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
 *   <li>It allows for inclusions, using the {@link #INCLUDES_FIELD} at the YAML document/file root level (ie, not 
 *   in a nested level. Optional inclusions are also supported, via {@link #INCLUDES_OPTIONAL_FIELD}.</li>
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
public class YAMLLoader
{
	public static final String INCLUDES_FIELD = "@includes";
	public static final String INCLUDES_OPTIONAL_FIELD = "@includes-optional";

	public static final String MERGE_SUFFIX = "@merge";
	public static final String PROPDEF_FIELD = "@properties";
	
	/**
	 * A property with this name is always available and contains the absolute YAML file path
	 */
	public static final String MY_PATH_PROP = "me";
	
	/**
	 * A property with this name is always available and contains the dir in which the YAML file is
	 */	
	public static final String MY_DIR_PROP = "mydir";
	
	private static Logger slog = LoggerFactory.getLogger ( YAMLLoader.class );

	
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
	 * @param filePath is used for the relative paths mentioned by the {@link #INCLUDES_FIELD includes} or
	 * {@link #INCLUDES_OPTIONAL_FIELD optional includes} directives. It can be null if such directives aren't used, 
	 * or are used with absolute paths only. 
	 * 
	 */
	private static <T> T loadYAMLFromString ( String yamlStr, Class<T> targetClass, String filePath ) 
		throws UncheckedIOException
	{
		Map<String, Object> yamlo = rawLoadingFromString ( yamlStr, new LinkedHashMap<> (), filePath, new StandardEnvironment () );
		var mapper = new ObjectMapper ();
		var result = mapper.convertValue ( yamlo, targetClass );
		
		return result;
	}
	
	
	private static Map<String, Object> rawLoadingFromFile ( 
		String filePath, Map<String, Object> resultJso, ConfigurableEnvironment interpolator 
	) throws UncheckedIOException
	{
		return rawLoadingFromString ( IOUtils.readFile ( filePath ), resultJso, filePath, interpolator );
	}
	
	/**
	 * Process the YAML recursively. That is, {@link #mergeJsObjects(Map, Map) merges recursively} the current YAML to 
	 * resultJso, and then calls itself (indirectly, via {@link #rawLoadingFromFile(String, Map)}) for each 
	 * {@link #INCLUDES_FIELD include} or {@link #INCLUDES_OPTIONAL_FIELD optional include}.
	 *   
	 */
	private static Map<String, Object> rawLoadingFromString ( 
		String yamlStr, Map<String, Object> resultJso, String filePath, ConfigurableEnvironment interpolator 
	) throws UncheckedIOException
	{
		// Interpolate ${variable}, can contain system properties or environment properties
		// TODO: SpEL
		//
		// First, add local properties, by creating a new locally-scoped interpolator, which inherits
		// the parent. Note that this is reentrant, the parent calls will see the original interpolator
		// ie, local property defs apply downwards only and can't change a parent file
		//
		var localInterpolator = collectProperties ( interpolator, yamlStr, filePath );
		
		// Then do the job
		yamlStr = localInterpolator.resolvePlaceholders ( yamlStr );

		// Now, re-parse YAML with interpolated values
		Map<String, Object> jsoTmp = parseYAML ( yamlStr, filePath );
		
		
		// OK, now we can process it against our rules
		//
		//
		
		// This has to be consumed and removed from the following processing.
		jsoTmp.remove ( PROPDEF_FIELD );
		
		
		// First, process all includes, so the inner-most inclusions can merge the ancestors
		// TODO: convert single value to array?
		//
		processIncludes ( jsoTmp, resultJso, localInterpolator, filePath, false );
		processIncludes ( jsoTmp, resultJso, localInterpolator, filePath, true );
		
		// Now resultJso is the merge of all the descendant inclusions, let's merge our own content
		mergeJsObjects ( jsoTmp, resultJso );	
		
		return resultJso;
	}
	
	/**
	 * Merges a JSON object into a parent, used for nested merges within included
	 * YAML (jso) and including one (parentJso). 
	 * 
	 * Note that includes are managed separately from this.
	 */
	@SuppressWarnings ( "unchecked" )
	private static void mergeJsObjects ( Map<String, Object> jso, Map<String, Object> targetJso )
	{
		for ( String key: jso.keySet () )
		{
			Object val = jso.get ( key );
			
			// Not my duty
			if ( INCLUDES_FIELD.equals ( key ) || INCLUDES_OPTIONAL_FIELD.equals ( key ) ) continue;
			else if ( key.matches ( ".+" + MERGE_SUFFIX + "\\s*$" ) )
			{
				// Merges lists into lists, or structured objects into structured objects
				//
				
				// First things first
				String actualKey = key.replaceFirst ( "\\s*" + MERGE_SUFFIX + "\\s*$", "" );
								
				// Collections, just merge to the parent, if it's a collection too 
				if ( val instanceof Collection )
				{
					Object targetVal = targetJso.computeIfAbsent ( actualKey, k -> new ArrayList<> () ); 

					if ( !( targetVal instanceof Collection ) )
						ExceptionUtils.throwEx ( IllegalArgumentException.class,
							"YAML error: %s directive used with an array-like field '%s', but child value isn't an array", 
							MERGE_SUFFIX, actualKey
					);

					((Collection<Object>) targetVal).addAll ( (Collection<Object>) val ); 
				}
				// Maps, dive down with recursion over the parent and current value
				else if ( val instanceof Map )
				{
					Object targetVal = targetJso.computeIfAbsent ( actualKey, k -> new HashMap<String, Object> () ); 
					
					if ( !( targetVal instanceof Map ) )
						ExceptionUtils.throwEx ( IllegalArgumentException.class,
							"YAML error: %s directive used with a map field '%s', but child value isn't a map", 
							MERGE_SUFFIX, actualKey
					);

					mergeJsObjects ( (Map<String, Object>) val, (Map<String, Object>) targetVal );
				}
				else
					// Merge asked, but it's not a mergeable type, meh!
					ExceptionUtils.throwEx ( IllegalArgumentException.class,
						"YAML error: %s directive used for the field %s, but the child field has a single plain value (must be object or array)", 
						MERGE_SUFFIX, actualKey
				);
			}
			else
				// No merge directive, just override
				targetJso.put ( key, val );
		} // for key
	}
	
	
	private static ConfigurableEnvironment collectProperties ( ConfigurableEnvironment parentEnv, String yamlStr, String filePath )
	{
		Map<String, Object> localProps = new HashMap<> ();
		
		// First the constant always-available vars
		//
		String myPath = getAbsolutePath ( filePath );
		if ( myPath != null ) localProps.put ( MY_PATH_PROP, myPath );
		
		String basePath = getBasePath ( filePath );
		if ( basePath != null ) localProps.put ( MY_DIR_PROP, basePath );
		
		// Next, any declared property
		// TODO: exclude constants above
		//
		Map<String, Object> jso = parseYAML ( yamlStr, filePath );
		Object declaredPropsObj = jso.get ( PROPDEF_FIELD );
		if ( declaredPropsObj != null ) 
		{
			if ( ! ( declaredPropsObj instanceof Map ) )
				ExceptionUtils.throwEx ( IllegalArgumentException.class, 
					"%s directive in %s doesn't contain property definitions", 
					PROPDEF_FIELD,
					fileLabel ( filePath )
			);
	
			@SuppressWarnings ( "unchecked" )
			Map<String, Object> declaredProps = (Map<String, Object>) declaredPropsObj;
			localProps.putAll ( declaredProps );
		}
		
		// A new local environment, with the parent plus the local variables
		//
		ConfigurableEnvironment newEnv = new StandardEnvironment ();
		MutablePropertySources propSrc = newEnv.getPropertySources ();
		propSrc.addFirst ( new MapPropertySource (  filePath, localProps ) );		
		newEnv.merge ( parentEnv );		
		
		return newEnv;
	}
	
	@SuppressWarnings ( "unchecked" )
	private static Map<String, Object> parseYAML ( String yamlStr, String filePath )
	{
		var mapper = new ObjectMapper ( new YAMLFactory () );
		try {
			return mapper.readValue ( yamlStr, LinkedHashMap.class );
		}
		catch ( JsonProcessingException ex ) {
			throw ExceptionUtils.buildEx ( UncheckedIOException.class, ex, 
				"Error while processing the YAML file: %s: $cause", fileLabel ( filePath )
			);
		}	
	}
	
	private static String fileLabel ( String filePath )
	{
		return Optional.ofNullable ( filePath )
			.map ( f -> '"' + f + '"' )
			.orElse ( "<N.A.>" );
	}
	
	/** TODO: move to its own utility class?! */
	
	private static String getAbsolutePath ( String filePath )
	{
		String absPath = filePath == null 
			? null 
			: Optional.ofNullable ( Path.of ( filePath ).toAbsolutePath () )
				.map ( Object::toString )
				.orElseThrow ( () -> ExceptionUtils.buildEx ( 
					IllegalArgumentException.class, "Invalid file path: %s", filePath )
		);
		
		return absPath;
	}
	
	/** TODO: move to its own utility class?! */	
	private static String getBasePath ( String filePath )
	{
		String basePath = filePath == null 
			? null 
			: Optional.ofNullable ( Path.of ( filePath ).toAbsolutePath ().getParent () )
				.map ( Object::toString )
				.orElseThrow ( () -> ExceptionUtils.buildEx ( 
					IllegalArgumentException.class, "Invalid file path: %s", filePath )
		);
		
		return basePath;
	}
	
	/**
	 * Processes {@link #INCLUDES_FIELD} or {@link #INCLUDES_OPTIONAL_FIELD}.
	 * 
	 * This is used by {@link #rawLoadingFromString(String, Map, String, ConfigurableEnvironment)}, to recursively
	 * load the files linked by these directives.
	 * 
	 * @param jso the JSON configuration being processes 
	 * @param targetJso the JSON configuration where jso and the results from includes directive are merged
	 * @param interpolator the property interpolator being currently used 
	 * @param yamlPath the file path being loaded (for log/error messages)
	 * @param isOptional if you want to process the optional includes or the mandatory ones.
	 */
	@SuppressWarnings ( "unchecked" )
	private static void processIncludes (
		Map<String, Object> jso, Map<String, Object> targetJso, ConfigurableEnvironment interpolator, 
		String yamlPath, boolean isOptional
	)
	{
		String fieldName = isOptional ? INCLUDES_OPTIONAL_FIELD : INCLUDES_FIELD;
		
		Object includesObj = jso.get ( fieldName );
		if ( includesObj == null ) return;

		if ( ! ( includesObj instanceof Collection ) ) ExceptionUtils.throwEx (
			IllegalArgumentException.class,
			"Error while loading YAML file \"%s\": %s directive must contain an array",
			fileLabel ( yamlPath ),
			fieldName
		);
			
		// Deal with relative paths
		// null is a rare event here (filePath should be a file), but SpotBugs recommended to check it
		//
		String basePath = getBasePath ( yamlPath );

		for ( String includePath: (Collection<String>) includesObj )
		{
			if ( !( basePath == null || includePath.startsWith ( "/" ) ) )
				includePath = basePath + "/" + includePath;
			
			if ( !Path.of ( includePath ).toFile ().exists () )
			{
				if ( !isOptional ) ExceptionUtils.throwEx (
					IllegalArgumentException.class,
					"Error while loading YAML file \"%s\": include \"%s\" doesn't exist, use %s for optional includes",
					fileLabel ( yamlPath ),
					includePath,
					fieldName
				);

				slog.info ( "Ignoring non-existent otpional include \"{}\"", includePath );
				continue;
			}
			rawLoadingFromFile ( includePath, targetJso, interpolator );
		}
	}
}
