package io.jaconi.jwkscache.persistence;

import static com.nimbusds.jose.jwk.source.JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT;
import static com.nimbusds.jose.jwk.source.JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT;
import static com.nimbusds.jose.jwk.source.JWKSourceBuilder.DEFAULT_HTTP_SIZE_LIMIT;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;

@Configuration
public class PersistenceConfig {

	@Bean
	@SuppressWarnings("unused")
	@ConditionalOnMissingBean(ResourceRetriever.class)
	public ResourceRetriever defaultResourceRetriever() {
		return new DefaultResourceRetriever(
				DEFAULT_HTTP_CONNECT_TIMEOUT,
				DEFAULT_HTTP_READ_TIMEOUT,
				DEFAULT_HTTP_SIZE_LIMIT);
	}
}
