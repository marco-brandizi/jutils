package uk.ac.ebi.utils.opt.config;

import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.env.StandardEnvironment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.opt.io.IOUtils;

/**
 * TODO: comment me!
 *
 * TODO: support for URLs
 * TODO: support for charsets?
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
	
	public static <T> T loadYAMLFromFile ( String filePath, Class<T> targetClass ) throws UncheckedIOException
	{
		return loadYAMLFromString ( IOUtils.readFile ( filePath ), targetClass, filePath );
	}

	public static <T> T loadYAMLFromString ( String yamlStr, Class<T> targetClass )
	{
		return loadYAMLFromString ( yamlStr, targetClass, null );
	}
	
	
	private static <T> T loadYAMLFromString ( String yamlStr, Class<T> targetClass, String filePath ) 
		throws UncheckedIOException
	{
		Map<String, Object> yamlo = rawLoadingFromString ( yamlStr, new HashMap<> (), filePath );
		var mapper = new ObjectMapper ();
		var result = mapper.convertValue ( yamlo, targetClass );
		
		return result;
	}
	
	
	private static Map<String, Object> rawLoadingFromFile ( String configFilePath, Map<String, Object> resultJso )
		throws UncheckedIOException
	{
		return rawLoadingFromString ( IOUtils.readFile ( configFilePath ), resultJso, configFilePath );
	}
	
	@SuppressWarnings ( "unchecked" )
	private static Map<String, Object> rawLoadingFromString ( 
		String yamlStr, Map<String, Object> resultJso, String filePath 
	)
		throws UncheckedIOException
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
			throw ExceptionUtils.buildEx ( UncheckedIOException.class, 
				"Error while processing the YAML file: \"%s\": $cause", Optional.ofNullable ( filePath ).orElse ( "<N.A.>" )
			);
		}
		
		// Includes have to be processed at the end, so that resultJso is populated with all the parent values
		for ( String key: new HashSet<> ( jso.keySet () ) )
		{
			Object val = jso.get ( key );

			// Managing inclusion directive
			if ( INCLUDES_PROP.equals ( key ) ) continue;
			// Managing merges of multi-value properties
			else if ( key.matches ( ".+" + MERGE_SUFFIX + "\\s*$" ) )
			{
				String actualKey = key.replaceFirst ( "\\s*" + MERGE_SUFFIX + "\\s*$", "" );
				
				Object parentVal = resultJso.computeIfAbsent ( actualKey, k -> new ArrayList<> () ); 
				
				if ( ! ( (val instanceof Collection ) && (parentVal instanceof Collection) ) )
					ExceptionUtils.throwEx ( IllegalArgumentException.class,
						"%s prefix used with the single-value property '%s', use arrays to define its values", 
						actualKey, MERGE_SUFFIX
				);

				((Collection<Object>) parentVal).addAll ( (Collection<Object>) val ); 
			}
			else
				resultJso.put ( key, val );
		}		
		
		
		// And now the includes
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
		String basePath = Path.of ( filePath ).toAbsolutePath ().getParent ().toString ();

		for ( String include: (Collection<String>) includesObj )
		{
			if ( !include.startsWith ( "/" ) ) include = basePath + "/" + include;
			Map<String, Object> jso1 = rawLoadingFromFile ( include, resultJso );
			jso.putAll ( jso1 );
		}
		
		return resultJso;
	}
	
}
