package io.jaconi.jwkscache;

import java.net.URL;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "jaconi.jwks")
public record JWKSProperties(List<URL> urls) {
}
