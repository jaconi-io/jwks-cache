package io.jaconi.jwkscache;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.ResourceRetriever;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JWKSConfig {
	private final HealthReportListenerFactory healthReportListenerFactory;
	private final ResourceRetriever resourceRetriever;
	private final JWKSProperties jwksProperties;

	@Bean
	public List<JWKSource<SecurityContext>> jwkSetSource() {
		return jwksProperties.urls().stream().map(this::jwkSource).toList();
	}

	private JWKSource<SecurityContext> jwkSource(URL url) {
		log.info("caching JWKS for endpoint {}", url);
		var builder = JWKSourceBuilder.create(url, resourceRetriever)
				.healthReporting(healthReportListenerFactory.create(url))
				.retrying(true);

		if (jwksProperties.caching().enabled()) {
			builder.cache(jwksProperties.caching().timeToLive().toMillis(), JWKSourceBuilder.DEFAULT_CACHE_REFRESH_TIMEOUT);
		} else {
			log.warn("caching should not be disabled in production");
			builder.cache(false);
			log.warn("disabling caching disabled rate limiting");
			builder.rateLimited(false);
			log.warn("disabling caching disabled refresh-ahead caching");
			builder.refreshAheadCache(false);
		}

		if (jwksProperties.outageTolerance().enabled()) {
			builder.outageTolerant(jwksProperties.outageTolerance().timeToLive().toMillis());
		} else {
			builder.outageTolerant(false);
		}

		var source = builder.build();

		// Warmup cache on startup.
		try {
			source.get(new JWKSelector(new JWKMatcher.Builder().build()), null);
		} catch (KeySourceException e) {
			log.warn("exception during cache warmup for {}", url, e);
		}

		return source;
	}
}
