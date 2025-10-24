package io.jaconi.jwkscache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.junit.jupiter.MockServerSettings;
import org.mockserver.model.HttpError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockServerExtension.class)
@MockServerSettings(ports = 8082)
@SpringBootTest(properties = {
		"jaconi.jwks.caching.enabled = false",
		"jaconi.jwks.outage-tolerance.enabled = false",
		"jaconi.jwks.urls = http://localhost:8082"
})
class OutageToleranceDisabledTest {
	private final ClientAndServer client;

	@Autowired
	private WellKnownController wellKnownController;

	public OutageToleranceDisabledTest(ClientAndServer client) {
		this.client = client;
	}

	@BeforeEach
	public void setUp() {
		client.reset();
	}

	@Test
	void testOutageTolerance() {
		client.when(request().withMethod("GET")).respond(response().withBody("""
				{
				  "keys": [
				    {
				      "e": "AQAB",
				      "use": "sig",
				      "kid": "fb9f9371d5755f3e383a40ab3a172cd8baca517f",
				      "n": "to2hcsFNHKquhCdUzXWdP8yxnGqxFWJlRT7sntBgp47HwxB9HFc-U_AB1JT8xe1hwDpWTheckoOfpLgo7_ROEsKpVJ_OXnotL_dgNwbprr-T_EFJV7qOEdHL0KmrnN-kFNLUUSqSChPYVh1aEjlPfXg92Yieaaz2AMMtiageZrKoYnrGC0z4yPNYFj21hO1x6mvGIjmpo6_fe91o-buZNzzkmYlGsFxdvUxYAvgk-5-7D10UTTLGh8bUv_BQT3aRFiVRS5d07dyCJ4wowzxYlPSM6lnfUlvHTWyPL4JysMGeu-tbPA-5QvwCdSGpfWFQbgMq9NznBtWb99r1UStpBQ",
				      "kty": "RSA",
				      "alg": "RS256"
				    }
				  ]
				}
				"""));

		var response = wellKnownController.jwksJSON();
		assertThat(response).isNotNull();
		assertThat(response).containsKey("keys");
		assertThat(response.get("keys")).hasSize(1);

		// Simulate an outage.
		client.reset();
		client.when(request().withMethod("GET")).error(HttpError.error().withDropConnection(true));

		// Empty response (caching and outage tolerance are disabled).
		response = wellKnownController.jwksJSON();
		assertThat(response).isNotNull();
		assertThat(response).containsKey("keys");
		assertThat(response.get("keys")).hasSize(0);
	}
}
