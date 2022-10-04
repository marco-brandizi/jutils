package uk.ac.ebi.utils.opt.yaml;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
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

import uk.ac.ebi.utils.opt.config.YAMLLoader;

/**
 * Several examples of use here, see also the test files in src/test/resources/yaml-utils
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Jun 2022</dd></dl>
 *
 */
public class YAMLLoaderTest
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	private static final String TEST_DATA_DIR = "target/test-classes/yaml-utils/";
	
	/**
	 * Example of POIO onto which we map some of the test files.
	 * 
	 * @see {@link YAMLLoaderTest#testMapping()}
	 *
	 */
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
		Map<String, Object> jso = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "basic.yml", HashMap.class );

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
	 * Shows how to use {@link YAMLLoader#INCLUDES_FIELD} to include a file from another.
	 * 
	 * The result is a merge of properties. If properties are repeated downstream, they override the parent's
	 * values, unless {@link YAMLLoader#MERGE_SUFFIX the merge directive} is used (see {@link #testInclusionMerge()}). 
	 */
	@Test
	@SuppressWarnings ( "unchecked" )
	public void testInclusion ()
	{
		Map<String, Object> jso = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "inclusion-main.yml", HashMap.class );
		assertEquals ( "app name is wrong!", "The Super Cool App", jso.get ( "app name" ) );
		assertEquals ( "options is wrong!", "default options", jso.get ( "options" ) );		
		assertEquals ( "'more options' is wrong!", "advanced options", jso.get ( "more options" ) );		
		assertEquals ( "'yet more options' is wrong!", "WTH you want", jso.get ( "yet more options" ) );
		assertEquals ( "Overridden version is wrong!", 3.0d, (double) jso.get ( "version" ), 0d );
	}

	/**
	 * Tests the use of the {@link YAMLLoader#MERGE_SUFFIX merge directive} to merge parent fields and fields in the included files.
	 * The merge behaviour for a field is enabled by appending this suffix to its name.
	 */
	@Test
	@SuppressWarnings ( "unchecked" )	
	public void testInclusionMerge ()
	{
		Map<String, Object> jso = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "merge-inclusion-main.yml", HashMap.class );
		log.debug ( "Result: {}", jso );
		
		assertEquals ( "name is wrong!", "The Super Cool App", jso.get ( "name" ) );
		
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
		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "merge-inclusion-main.yml", TestTarget.class );

		log.info ( "Result: {}", cfg );
		
		assertEquals ( "name is wrong!", "The Super Cool App", cfg.getName () );
		assertEquals ( "version is wrong!", (Double) 2.5, cfg.getVersion () );
		
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
		YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "mapping-unused-fields.yml", TestTarget.class );
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

		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "interpolation.yml", TestTarget.class );

		log.info ( "Result: {}", cfg );
		
		assertEquals ( "name is wrong!", format ( "The %s App", testName ), cfg.getName () );
		assertEquals ( "version is wrong!", 2.5d, (double) cfg.getVersion (), 0d );
		
		var expectedOpts = Set.of ( "default " + optionsName, "advanced " + optionsName );
		assertEquals ( "'options' is wrong!", expectedOpts, cfg.getOptions () );			 
	}

	/**
	 * This is an example of {@link YAMLLoader#MERGE_SUFFIX merge option} applied to nested objects and 
	 * also applied to nested array fields.
	 * 
	 * See the test files nested-mapping.yml.
	 */
	@Test
	public void testNestedMapping ()
	{
		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "nested-mapping.yml", TestTarget.class );
		log.info ( "Result: {}", cfg );
		assertEquals ( "Wrong name!", "The Super Cool App", cfg.getName () );
		assertEquals ( "Wrong version!", 2.5d, cfg.getVersion (), 0d );
		
		TestTarget child = cfg.getChild ();
		assertEquals ( "Wrong child.name!", "The Child App", child.getName () );
		assertEquals ( "Wrong child.version!", 6.0, child.getVersion (), 0d );
	
		var expectedOpts = Set.of ( "default child options", "advanced child options" );
		assertEquals ( "Wrong child.options!", expectedOpts, child.getOptions () );			 
	}
	
	/**
	 * You can use the {@link YAMLLoader#PROPDEF_FIELD} field to define variables. See the custom-prop.yml
	 * example.
	 */
	@Test
	public void testCustomProperties ()
	{
		// Properties can come from either the Java properties (-D) or the environment.
		var testName = System.getenv ( "yamlUtils_testName" );
		
		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "custom-props.yml", TestTarget.class );

		log.info ( "Result: {}", cfg );
		
		assertEquals ( "name is wrong!", format ( "The %s App", testName ), cfg.getName () );
		assertEquals ( "version is wrong!", 2.5d, (double) cfg.getVersion (), 0d );
		
		var expectedOpts = Set.of ( "default options" );
		assertEquals ( "'options' is wrong!", expectedOpts, cfg.getOptions () );			 
	}

	/**
	 * Variables are inherited by included files, but not vice versa, see the example.
	 */
	@Test
	public void testCustomPropertiesInclusions ()
	{		
		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "custom-props-inclusions.yml", TestTarget.class );

		log.info ( "Result: {}", cfg );
		
		// The localVar placeholder are left unchanged, cause it's defined in the included file only
		assertEquals ( "name is wrong!", "The Nice App ${localVar}", cfg.getName () );
		
		// This is re-defined in the included file, but the upper level wins
		assertEquals ( "version is wrong!", 2.0d, (double) cfg.getVersion (), 0d );
		
		// This is composed by a mix of upper-level and local variables/values. The value is added to the
		// top-level object, since the including file doesn't mention it.
		assertEquals ( 
			"description is wrong!",
			"The Nice App: Description from the included file. Default version: 1.0",
			cfg.getDescription ()
		);
	}
	
	/**
	 * You can use certain default properties for interpolation, see {@link YAMLLoader}.
	 */
	@Test
	public void testDefaultProps ()
	{
		var cfgName = "default-props.yml";
		var includedName = "default-props-1.yml";
		
		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + cfgName, TestTarget.class );
		
		var expectedOpts = Set.of (
			"Top file is " + Path.of ( TEST_DATA_DIR, cfgName ).toAbsolutePath ().toString (),
			"Top dir is " + Path.of ( TEST_DATA_DIR ).toAbsolutePath ().toString (),
			"Included file is " + Path.of ( TEST_DATA_DIR, "subdir", includedName ).toAbsolutePath ().toString (),
			"Included dir is " + Path.of ( TEST_DATA_DIR, "subdir" ).toAbsolutePath ().toString ()
		);
		assertEquals ( "'options' is wrong!", expectedOpts, cfg.getOptions () );			 
	}
	
	/**
	 * the {@link YAMLLoader#INCLUDES_OPTIONAL_FIELD} can be used to have optional inclusions, ie, included files which are
	 * actually loaded if they exists.
	 * 
	 * In particular, this can be useful to define files that contain an optional inclusion like
	 * config-local.yml. A file like this can be auto-updated without loosing the specific user-provided
	 * configuration for an app.
	 * 
	 */
	@Test
	public void testOptionalInclusion ()
	{
		TestTarget cfg = YAMLLoader.loadYAMLFromFile ( TEST_DATA_DIR + "optional-inclusion.yml", TestTarget.class );
		log.info ( "Result: {}", cfg );
		assertEquals ( "Wrong name!", "The Super Cool App", cfg.getName () );
		assertEquals ( "Wrong version!", 2.5d, cfg.getVersion (), 0d );
		
		TestTarget child = cfg.getChild ();
		assertEquals ( "Wrong child.name!", "The Child App", child.getName () );
		assertEquals ( "Wrong child.version!", 6.0, child.getVersion (), 0d );
	
		var expectedOpts = Set.of ( "default child options", "advanced child options" );
		assertEquals ( "Wrong child.options!", expectedOpts, child.getOptions () );			 
	}
	
}
