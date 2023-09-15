package io.jaconi.jwkscache;

import java.net.URL;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties(value = "jaconi.jwks")
public record JWKSProperties(@NotEmpty List<URL> urls) {
}
