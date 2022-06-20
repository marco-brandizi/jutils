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
import org.springframework.core.env.StandardEnvironment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.ebi.utils.opt.config.YAMLUtils;

/**
 * Several examples of use here, see also the test files in src/test/resources/yaml-utils
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Jun 2022</dd></dl>
 *
 */
public class YAMLUtilsTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	private static final String TEST_DATA_DIR = "target/test-classes/yaml-utils/";
	
	/**
	 * Example of POIO onto which we map some of the test files.
	 * 
	 * @see {@link YAMLUtilsTest#testMapping()}
	 *
	 */
	@SuppressWarnings ( "unused" )
	private static class TestTarget
	{
		private String name, description;
		private Double version;
		private Set<String> options;
		
		private TestTarget child;
		
		// You don't need any setter (or getter, for what matters), the Jackson libs 
		// use reflection to find private fields.
		//
		
		public String getName ()
		{
			return name;
		}
		public String getDescription ()
		{
			return description;
		}
		public Double getVersion ()
		{
			return version;
		}
		public Set<String> getOptions ()
		{
			return options;
		}
		public TestTarget getChild ()
		{
			return child;
		}
		
		
		@Override
		public String toString ()
		{
			return String.format ( 
				"TestTarget{name: %s, description: %s, version: %s, options: %s, child: %s}", 
				name, description, version, options, child 
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
	
	/**
	 * Shows how to use {@link YAMLUtils#INCLUDES_PROP} to include a file from another.
	 * 
	 * The result is a merge of properties. If properties are repeated downstream, they override the parent's
	 * values, unless {@link YAMLUtils#MERGE_SUFFIX the merge directive} is used (see {@link #testInclusionMerge()}). 
	 */
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

	/**
	 * Tests the use of the {@link YAMLUtils#MERGE_SUFFIX merge directive} to merge parent fields and fields in the included files.
	 * The merge behaviour for a field is enabled by appending this suffix to its name.
	 */
	@Test
	@SuppressWarnings ( "unchecked" )	
	public void testInclusionMerge ()
	{
		Map<String, Object> jso = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "merge-inclusion-main.yml", HashMap.class );
		log.debug ( "Result: {}", jso );
		
		assertEquals ( "app name is wrong!", "The Super Cool App", jso.get ( "name" ) );
		
		// A mix of 'options' and 'options @merge' is used in the files
		Set<String> opts = new HashSet<> ( (Collection<String> ) jso.get ( "options" ) );
		var expectedOpts = Set.of ( "default options", "advanced options", "looking-for-trouble options" );
		assertEquals ( "'options' is wrong!", expectedOpts, opts );		
	}

	/**
	 * Mapping to POJO. This is one of the main usage. Note that the mapping happens after inclusions, merges and interpolations.
	 */
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
	
	/**
	 * You can leverage {@link StandardEnvironment Spring interpolation} to solve expressions like 
	 * <tt>${propName}</tt>. The names are resolved from Java properties or enviornment variables. 
	 */
	@Test
	public void testInterpolation ()
	{
		// Properties can come from either the Java properties (-D) or the environment.
		var testName = System.getenv ( "yamlUtils_testName" );
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

	/**
	 * This is an example of {@link YAMLUtils#MERGE_SUFFIX merge option} applied to nested objects and 
	 * also applied to nested array fields.
	 * 
	 * See the test files nested-mapping.yml.
	 */
	@Test
	public void testNestedMapping ()
	{
		TestTarget cfg = YAMLUtils.loadYAMLFromFile ( TEST_DATA_DIR + "nested-mapping.yml", TestTarget.class );
		log.info ( "Result: {}", cfg );		
	}
}
