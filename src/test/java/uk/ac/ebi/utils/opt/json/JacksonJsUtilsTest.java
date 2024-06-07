package uk.ac.ebi.utils.opt.json;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>6 Jun 2024</dd></dl>
 *
 */
public class JacksonJsUtilsTest
{
	@SuppressWarnings ( "unused" )
	private static class Foo
	{
		private String name;
		private double value;

		private Foo () {}
		
		public Foo ( String name, double value )
		{
			this.name = name;
			this.value = value;
		}

		public String getName ()
		{
			return name;
		}

		public double getValue ()
		{
			return value;
		}

		@Override
		public int hashCode ()
		{
			return Objects.hash ( name, value );
		}

		@Override
		public boolean equals ( Object obj )
		{
			if ( this == obj ) return true;
			if ( ! ( obj instanceof Foo ) ) return false;
			Foo other = (Foo) obj;
			return Objects.equals ( name, other.name )
					&& Double.doubleToLongBits ( value ) == Double.doubleToLongBits ( other.value );
		}
	}
	
	@Test
	public void testToArrayNode ()
	{
		ObjectMapper omapper = new ObjectMapper (); 
		var refjs = omapper.createObjectNode ();

		Set<String> testVals = Set.of ( "Hello", "World"  );
		
		ArrayNode jsa = testVals.stream ()
		.map ( refjs::textNode )
		.collect ( JacksonJsUtils.toArrayNode ( omapper ) );
		
		assertEquals ( "JSON array is wrong!",
			testVals,
			// Reconstructs the original array from JSON
			Optional.ofNullable ( jsa )
			.map ( ArrayNode::spliterator )
			.map ( spl -> 
				StreamSupport.stream ( spl, false )
				.map ( JsonNode::asText )
				.collect ( Collectors.toSet () )
			)
			.orElse ( null )
		);
	}
	
	@Test
	public void testToArrayNodeObj ()
	{
		ObjectMapper omapper = new ObjectMapper ();

		Set<Foo> testVals = Set.of ( 
			new Foo ( "Hello", 0d ), new Foo ( "World", 0.5 ) );
		
		ArrayNode jsa = testVals.stream ()
		.map ( foo -> omapper.convertValue ( foo, JsonNode.class ) )
		.collect ( JacksonJsUtils.toArrayNode ( omapper ) );
		
		assertEquals ( "JSON array is wrong!",
			testVals, 
			Optional.ofNullable ( jsa )
			.map ( ArrayNode::spliterator )
			.map ( spl -> 
				StreamSupport.stream ( spl, false )
				.map ( js -> omapper.convertValue ( js, Foo.class ) )
				.collect ( Collectors.toSet () )
			)
			.orElse ( null )
		);
	}
	
	
	@Test
	public void testToObjectNode ()
	{
		ObjectMapper omapper = new ObjectMapper ();
		var refjs = omapper.createObjectNode ();

		Map<String, Double> testVals = Map.of ( 
			"Hello", 0d, "World", 0.5 
		);

		ObjectNode jsn = testVals.entrySet ()
		.stream ()
		.collect ( JacksonJsUtils.toObjectNode ( 
			omapper,
			Map.Entry::getKey, 
			e -> refjs.numberNode ( e.getValue () )
		));
		
		assertEquals ( "JSON object is wrong!",
			testVals, 
			Optional.ofNullable ( jsn )
			.map ( js -> omapper.convertValue ( 
				js, new TypeReference<Map<String, Double>>(){/**/} ) 
			)
			.orElse ( null )
		);
		
	}
	
	/**
	 * The default object merger forbids multiple values per key. 
	 */
	@Test ( expected = IllegalArgumentException.class )
	public void testToObjectFailOnValueClash ()
	{
		ObjectMapper omapper = new ObjectMapper ();
		var refjs = omapper.createObjectNode ();

		Map<String, Double> testVals = Map.of ( 
			"Hello", 0d, "Hello", 0.5 
		);

		testVals.entrySet ()
		.stream ()
		.collect ( JacksonJsUtils.toObjectNode ( 
			omapper,
			Map.Entry::getKey, 
			e -> refjs.numberNode ( e.getValue () )
		));
	}
	
	/**
	 * Custom merger that does something with key-clashing values.
	 */
	@Test
	public void testToObjectWithMerger ()
	{
		ObjectMapper omapper = new ObjectMapper ();
		var refjs = omapper.createObjectNode ();

		List<Pair<String, Double>> entries = List.of ( 
		  Pair.of ( "Hello", 1d ),
		  Pair.of ( "Hello", 2d ),
		  Pair.of ( "Same", 0d ),
		  Pair.of ( "Same", 0d ),
		  Pair.of ( "Foo", 3d )
		);
		
		Map<String, Double> testVals = Map.of ( 
			"Hello", (1d + 2d)/2, "Same", 0d, "Foo", 3d
		);

		JsonNode jsn = entries
		.stream ()
		.collect ( JacksonJsUtils.toObjectNode ( 
			omapper,
			Pair::getKey, 
			e -> refjs.numberNode ( e.getValue () ),
			(js1, js2) -> {
				double v1 = js1.asDouble ();
				double v2 = js2.asDouble ();
				if ( v1 == v2 ) return js1;
				return refjs.numberNode ( (v1 + v2) / 2d );
			}
		));
		
		assertEquals ( "JSON object is wrong!",
			testVals, 
			Optional.ofNullable ( jsn )
			.map ( js -> omapper.convertValue ( 
				js, new TypeReference<Map<String, Double>>(){/**/} ) 
			)
			.orElse ( null )
		);
	}	
}
