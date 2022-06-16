package uk.ac.ebi.utils.opt.yaml;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.utils.opt.config.YAMLUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Jun 2022</dd></dl>
 *
 */
public class YAMLUtilsTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	private static final String TEST_DATA_DIR = "target/test-classes/yaml-utils/";
	
	@SuppressWarnings ( "unused" )
	private static class TestTarget
	{
		private String name, description;
		private Double version;
		private Set<String> options;
		
		public String getName ()
		{
			return name;
		}
		public void setName ( String name )
		{
			this.name = name;
		}
		public String getDescription ()
		{
			return description;
		}
		public void setDescription ( String description )
		{
			this.description = description;
		}
		public Double getVersion ()
		{
			return version;
		}
		public void setVersion ( Double version )
		{
			this.version = version;
		}
		public Set<String> getOptions ()
		{
			return options;
		}
		public void setOptions ( Set<String> options )
		{
			this.options = options;
		}
		@Override
		public String toString ()
		{
			return String.format ( 
				"TestTarget{name: %s, description: %s, version: %s, options: %s}", 
				name, description, version, options 
			);
		}
		
		
	}
	
	
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testBasicMapping ()
	{
		Map<String, Object> jso = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "basic.yml", HashMap.class );

		assertEquals ( "name is wrong!", "Jon", jso.get ( "name" ) );
		assertEquals ( "surname is wrong!", "Doe", jso.get ( "surname" ) );

		var addresses = (List<Map<String, Object>>) jso.get ( "addresses" );
		assertNotNull ( "addresses is null!", addresses );
		assertEquals ( "addresses is wrong!", 2, addresses.size () );
		
		String street = addresses.stream ()
		.map ( a -> (String) a.get ( "town" ) )
		.filter ( s -> "Somewhereland".equals ( s ) )
		.findAny ()
		.orElse ( null );
		
		assertNotNull ( "Probe address not found!", street );
	}
	
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testInclusion ()
	{
		Map<String, Object> jso = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "inclusion-main.yml", HashMap.class );
		assertEquals ( "app name is wrong!", "The Super Cool App", jso.get ( "app name" ) );
		assertEquals ( "options is wrong!", "default options", jso.get ( "options" ) );		
		assertEquals ( "'more options' is wrong!", "advanced options", jso.get ( "more options" ) );		
		assertEquals ( "'yet more options' is wrong!", "WTH you want", jso.get ( "yet more options" ) );		
	}

	@Test
	@SuppressWarnings ( "unchecked" )	
	public void testInclusionMerge ()
	{
		Map<String, Object> jso = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "merge-inclusion-main.yml", HashMap.class );
		log.debug ( "Result: {}", jso );
		
		assertEquals ( "app name is wrong!", "The Super Cool App", jso.get ( "name" ) );
		
		Set<String> opts = new HashSet<> ( (Collection<String> ) jso.get ( "options" ) );
		var expectedOpts = Set.of ( "default options", "advanced options", "looking-for-trouble options" );
		assertEquals ( "'options' is wrong!", expectedOpts, opts );		
	}

	
	@Test
	public void testMapping ()
	{
		TestTarget cfg = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "merge-inclusion-main.yml", TestTarget.class );

		log.info ( "Result: {}", cfg );
		
		assertEquals ( "app name is wrong!", "The Super Cool App", cfg.getName () );
		assertEquals ( "app name is wrong!", (Double) 2.5, cfg.getVersion () );
		
		var expectedOpts = Set.of ( "default options", "advanced options", "looking-for-trouble options" );
		assertEquals ( "'options' is wrong!", expectedOpts, cfg.getOptions () );		
	}
	
	/**
	 * {@link ObjectMapper} throws errors if a YAML field can't be mapped to the target POJO.
	 * TODO: should we more liberal, via {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES}?
	 */
	@Test ( expected = IllegalArgumentException.class )
	public void testMappingWithUnusedFields ()
	{
		// We expect it to work, despite the file has a non-mapped field.
		// So, if there is no exception is thrown, we're done.
		YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "mapping-unused-fields.yml", TestTarget.class );
	}
	
	@Test
	public void testInterpolation ()
	{
		// Properties can come from either the Java properties (-D) or the environment.
		var testName = System.getenv ( "yamlUtils.testName" );
		var sysp = System.getProperties ();
		var optionsName = "options";
		
		sysp.setProperty ( "testPrefix", "interpolation" );
		sysp.setProperty ( "optionsName", optionsName );

		TestTarget cfg = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "interpolation.yml", TestTarget.class );

		log.info ( "Result: {}", cfg );
		
		assertEquals ( "app name is wrong!", format ( "The %s App", testName ), cfg.getName () );
		assertEquals ( "app name is wrong!", (Double) 2.5, cfg.getVersion () );
		
		var expectedOpts = Set.of ( "default " + optionsName, "advanced " + optionsName );
		assertEquals ( "'options' is wrong!", expectedOpts, cfg.getOptions () );			 
	}

}
