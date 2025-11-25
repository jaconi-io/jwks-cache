package io.jaconi.jwkscache.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.util.Resource;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
class ResourceMixinTest {

	@Autowired
	JsonMapper jsonMapper;

	@Test
	void testSerialization() throws JsonProcessingException {
		var content = """
				{
				  "keys": [
				    { "kty": "RSA" }
				  ]
				}""";
		var res = new Resource(content, "application/json");
		var actual = jsonMapper.writeValueAsString(res);
		var expected = "{\"content\":" + content + ",\"contentType\":\"application/json\"}";
		assertEquals(expected, actual);
	}

	@Test
	void testDeserialization() throws JsonProcessingException {
		var json = """
				{
				  "content": {
				    "keys": [
				      { "kty":"RSA" }
				    ]
				  },
				  "contentType": "application/json"
				}""";
		var actual = jsonMapper.readValue(json, Resource.class);
		var expected = new Resource("{\"keys\":[{\"kty\":\"RSA\"}]}", "application/json");
		assertEquals(expected.getContentType(), actual.getContentType());
		assertEquals(expected.getContent(), actual.getContent());
	}
}
