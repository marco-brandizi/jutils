package uk.ac.ebi.utils.opt.json;

import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;
import static java.util.stream.Collector.Characteristics.UNORDERED;

import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

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
	 * @param objectMapper the mapper where the {@link ObjectMapper#createArrayNode()} is taken.
	 * @param accumulator method to add an element to a node. This is typically an addXXX() method
	 * from {@link ArrayNode}.
	 * 
	 * TODO: tests
	 * 
	 */
	public static <T> Collector<T, ArrayNode, ArrayNode> toArrayNode ( 
		ObjectMapper objectMapper, BiConsumer<ArrayNode, T> accumulator
	)
	{
		return Collector.<T, ArrayNode> of (
			objectMapper::createArrayNode,
			accumulator,
			ArrayNode::addAll,
			UNORDERED
		);
	}
	
	/**
	 * Flavour that uses {@link ArrayNode#add(JsonNode)} 
	 */
	public static Collector<JsonNode, ArrayNode, ArrayNode> toArrayNode ( 
		ObjectMapper objectMapper
	)
	{
		return toArrayNode ( objectMapper, ArrayNode::add );
	}	
}
