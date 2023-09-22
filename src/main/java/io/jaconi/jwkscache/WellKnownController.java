package io.jaconi.jwkscache;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WellKnownController {
	private static final JWKSelector SELECTOR = new JWKSelector(new JWKMatcher.Builder().build());
	private final List<JWKSource<SecurityContext>> jwkSources;

	@ResponseBody
	@GetMapping("/.well-known/jwks.json")
	public Map<String, List<Map<String, Object>>> jwksJSON() {
		var keys = jwkSources.stream()
				.map(src -> {
					try {
						return src.get(SELECTOR, null);
					} catch (KeySourceException e) {
						// Just fall back to an empty list. Health reporting will take care of logging.
						return Collections.<JWK>emptyList();
					}
				})
				.flatMap(Collection::stream)
				.map(JWK::toJSONObject)
				.toList();
		return Map.of("keys", keys);
	}
}
