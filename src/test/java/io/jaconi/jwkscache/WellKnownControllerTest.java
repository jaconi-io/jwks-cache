package io.jaconi.jwkscache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class WellKnownControllerTest {

	@Mock
	JWKSource<SecurityContext> source1;

	@Mock
	JWKSource<SecurityContext> source2;

	@Mock
	JWK jwk1;

	@Mock
	JWK jwk2;

	@Test
	void jwksJSON() throws KeySourceException {
		when(jwk1.toJSONObject()).thenReturn(Map.of("kty", "RSA", "kid", "1"));
		when(jwk2.toJSONObject()).thenReturn(Map.of("kty", "RSA", "kid", "2"));

		doThrow(new KeySourceException("expected"))
				.when(source1)
				.get(any(), any());

		doReturn(List.of(jwk1, jwk2))
				.when(source2)
				.get(any(), any());

		var controller = new WellKnownController(List.of(source1, source2));

		StepVerifier.create(controller.jwksJSON())
				.assertNext(res -> assertThat(res).containsExactly(entry("keys", List.of(
						Map.of("kty", "RSA", "kid", "1"),
						Map.of("kty", "RSA", "kid", "2")
				))))
				.verifyComplete();
	}
}
