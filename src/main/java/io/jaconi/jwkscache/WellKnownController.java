package io.jaconi.jwkscache;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WellKnownController {
	private static final JWKSelector SELECTOR = new JWKSelector(new JWKMatcher.Builder().build());
	private final List<JWKSource<SecurityContext>> jwkSources;

	@ResponseBody
	@GetMapping("/.well-known/jwks.json")
	public Mono<Map<String, List<Map<String, Object>>>> jwksJSON() {
		return Flux.fromIterable(jwkSources)
				.flatMap(src -> {
					try {
						return Flux.fromIterable(src.get(SELECTOR, null));
					} catch (KeySourceException e) {
						// Just fall back to an empty list. Health reporting will take care of logging.
						return Flux.empty();
					}
				})
				.map(JWK::toJSONObject)
				.collectList()
				.map(keys -> Map.of("keys", keys));
	}
}
