package io.jaconi.jwkscache.persistence;

import org.springframework.boot.jackson.JacksonMixin;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.nimbusds.jose.util.Resource;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * {@link JacksonMixin} used by the {@link tools.jackson.databind.json.JsonMapper} when serializing and deserializing
 * {@link Resource} instances.
 * <p>
 * The additional {@link JsonRawValue} annotation on {@link #content} ensures, that the JSON Web Key Store (JWKS) is
 * serialized as a JSON object (instead of an escaped {@link String}).
 * <p>
 * The {@link ResourceDeserializer} handles deserialization of the {@link #content}, despite {@link Resource} lacking
 * a default constructor.
 */
@JacksonMixin(Resource.class)
@JsonDeserialize(using = ResourceMixin.ResourceDeserializer.class)
public class ResourceMixin {

	@JsonRawValue
	@SuppressWarnings("unused")
	private String content;

	static class ResourceDeserializer extends ValueDeserializer<Resource> {

		@Override
		public Resource deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
			var intermediate = p.readValueAs(IntermediateResource.class);
			return new Resource(intermediate.content().toString(), intermediate.contentType());
		}
	}

	private record IntermediateResource(JsonNode content, String contentType) {
	}
}
