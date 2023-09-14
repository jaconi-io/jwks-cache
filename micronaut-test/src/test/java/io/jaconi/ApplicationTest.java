package io.jaconi;

import org.junit.jupiter.api.Test;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.restassured.specification.RequestSpecification;

@MicronautTest
class ApplicationTest {

	@Test
	void test(RequestSpecification spec) {
		spec.when().get("/test").then().statusCode(401);
	}
}
