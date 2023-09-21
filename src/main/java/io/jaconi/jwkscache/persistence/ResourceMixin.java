package io.jaconi.jwkscache.persistence;

import java.io.IOException;

import org.springframework.boot.jackson.JsonMixin;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nimbusds.jose.util.Resource;

/**
 * {@link JsonMixin} used by the {@link ObjectMapper} when serializing and deserializing {@link Resource} instances.
 * <p>
 * The additional {@link JsonRawValue} annotation on {@link #content} ensures, that the JSON Web Key Store (JWKS) is
 * serialized as a JSON object (instead of an escaped {@link String}).
 * <p>
 * The {@link ResourceDeserializer} handles deserialization of the {@link #content}, despite {@link Resource} lacking
 * a default constructor.
 */
@JsonMixin(Resource.class)
@JsonDeserialize(using = ResourceMixin.ResourceDeserializer.class)
public class ResourceMixin {

	@JsonRawValue
	@SuppressWarnings("unused")
	private String content;

	static class ResourceDeserializer extends JsonDeserializer<Resource> {
		@Override
		public Resource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
			var intermediate = p.readValueAs(IntermediateResource.class);
			return new Resource(intermediate.content().toString(), intermediate.contentType());
		}
	}

	private record IntermediateResource(JsonNode content, String contentType) {
	}
}
