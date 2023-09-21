package io.jaconi.jwkscache;

import java.net.URL;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JWKSConfig {
	private final HealthReportListenerFactory healthReportListenerFactory;

	@Bean
	public List<JWKSource<SecurityContext>> jwkSetSource(JWKSProperties jwksProperties) {
		return jwksProperties.urls().stream().map(this::jwkSource).toList();
	}

	private JWKSource<SecurityContext> jwkSource(URL url) {
		log.info("caching JWKS for endpoint {}", url);
		var source = JWKSourceBuilder.create(url)
				.healthReporting(healthReportListenerFactory.create(url))
				.retrying(true)
				// Handle outages of the JWKS source for up to 50 Minutes (default).
				.outageTolerant(true)
				.build();

		// Warmup cache on startup.
		try {
			source.get(new JWKSelector(new JWKMatcher.Builder().build()), null);
		} catch (KeySourceException e) {
			log.warn("exception during cache warmup for {}", url, e);
		}

		return source;
	}
}
