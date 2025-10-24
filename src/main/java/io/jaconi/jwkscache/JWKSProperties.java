package io.jaconi.jwkscache;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.net.URL;
import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(value = "jaconi.jwks")
public record JWKSProperties(
		@DefaultValue Caching caching,
		@DefaultValue OutageTolerance outageTolerance,
		@NotEmpty List<URL> urls
) {
	public record Caching(@DefaultValue("true") boolean enabled, @DefaultValue("5m") Duration timeToLive) {
	}

	public record OutageTolerance(@DefaultValue("true") boolean enabled, @DefaultValue("50m") Duration timeToLive) {
	}
}
