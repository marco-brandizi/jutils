package uk.ac.ebi.utils.opt.json;

import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.TriConsumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON utils based on the Jackson library.
 *
 * @author Marco Brandizi
 * <dl><dt>Date:</dt><dd>13 May 2024</dd></dl>
 *
 */
public class JacksonJsUtils
{
	/**
	 * Returns a collector (i.e., for {@link Stream#collect(Collector)} and the like) that
	 * collects the elements of type T into an {@link ArrayNode}.
	 * 
	 * This is a serialised collector, which requires synchronisation when elements are accumulated (ie, 
	 * it doesn't have {@link Characteristics#CONCURRENT}.
	 * 
	 * At the same time, it's an {@link Characteristics#UNORDERED} and {@link Characteristics#IDENTITY_FINISH}
	 * collector.
	 * 
	 * <p><b>Note</b>: to create JSON nodes out of plain value:
	 * 
	 * <pre>
	 * vat nodeFactory = jsmapperRO.getDeserializationConfig ().getNodeFactory ();
	 * nodeFactory.textNode ( "Hello, World" );
	 * nodeFactory.numericNode ( 2.5 );
	 * </pre>
	 * </p>
	 * 
	 * This can be used in a {@link Stream#map(Function) map step}.
	 * 
	 * @param objectMapper the Jackson object mapper, used to {@link ObjectMapper#createArrayNode() create a new array}.
	 * 
	 * 
	 */	
	public static Collector<JsonNode, ArrayNode, ArrayNode> toArrayNode ( ObjectMapper objectMapper )
	{
		return Collector.<JsonNode, ArrayNode> of (
			objectMapper::createArrayNode,
			(jsa, jselem) -> jsa.add ( jselem ),
			ArrayNode::addAll,
			UNORDERED
		);		
	}
	
	
	
	/**
	 * Collector to convert a stream of elements into an {@link ObjectNode}, by extracting
	 * keys and JSON mappings from the elements.
	 * 
	 * @param <T> the kind of element (in the stream)
	 * 
	 * @param objectMapper The usual Jackson JSON object mapper, used to create the
	 * return value via {@link ObjectMapper#createObjectNode()}
	 *  
	 * @param keyMapper maps an element to its key
	 * 
	 * @param valueMapper maps an element to its corresponding JSON.
	 * See notes on {@link #toArrayNode(ObjectMapper)} regarding how to convert plain values to JSON objects.
	 * 
	 * @param valuesMerger decides what to do in case of two values coming from the same key. This is the
	 * conceptual equivalent of the remapping function in {@link Map#merge(Object, Object, java.util.function.BiFunction)}.
	 * In the {@link #toObjectNode(ObjectMapper, Function, Function) default case}, it forbids to have
	 * duplicated keys with different values. 
	 */
	public static <T> Collector<T, ObjectNode, ObjectNode> toObjectNode (
		ObjectMapper objectMapper,
		Function<T, String> keyMapper,
		Function<T, JsonNode> valueMapper,
		BinaryOperator<JsonNode> valuesMerger
	)
	{		
		TriConsumer<ObjectNode, String, JsonNode> jsValMerge = (js, k, jsCurVal) ->
		{
			JsonNode jsOldVal = js.get ( k );
			JsonNode jsNewVal = jsOldVal == null
				? jsCurVal : valuesMerger.apply ( jsOldVal, jsCurVal );
			if ( jsNewVal == null )
				js.remove ( k );
			else
				js.set ( k, jsNewVal );
		};
		
		BiConsumer<ObjectNode, T> jsAccumulator = (js, e) ->
		{
			String k = keyMapper.apply ( e );
			jsValMerge.accept ( js, k, valueMapper.apply ( e ) );
		};
		
		BinaryOperator<ObjectNode> jsMerger = (js1, js2) ->
		{
			js2.fieldNames ()
			.forEachRemaining ( k -> jsValMerge.accept ( js1, k, js2.get ( k ) ));
			return js1;
		};
		
		return Collector.of ( 
			objectMapper::createObjectNode, jsAccumulator, jsMerger, UNORDERED 
		);
	}

	/**
	 * Uses a value merger that throws {@link IllegalArgumentException} when 
	 * the same key yields different values.
	 * 
	 */
	public static <T> Collector<T, ObjectNode, ObjectNode> toObjectNode (
		ObjectMapper objectMapper,
		Function<T, String> keyMapper,
		Function<T, JsonNode> valueMapper
	)
	{
		return toObjectNode (
			objectMapper,
			keyMapper,
			valueMapper,
			(js1, js2) ->
			{
				if ( js1 == null ) {
					if ( js2 == null ) return null;
				}
				else if ( js1.equals ( js2 ) ) return js1;
				
				throw new IllegalArgumentException ( 
					"toObjectNode(), merge of keys with multiple values not supported here"
				);
			}
		);
	}
}
