package io.jaconi.jwkscache;

import java.net.URL;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JWKSConfig {

	@Bean
	public List<JWKSource<SecurityContext>> jwkSetSource(JWKSProperties jwksProperties) {
		return jwksProperties.urls().stream().map(this::jwkSource).toList();
	}

	private JWKSource<SecurityContext> jwkSource(URL url) {
		return JWKSourceBuilder.create(url)
				.retrying(true)
				// Handle outages of the JWKS source for up to 50 Minutes (default).
				.outageTolerant(true)
				.build();
	}
}
