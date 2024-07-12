package io.jaconi.jwkscache.persistence;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@ConditionalOnProperty("jaconi.jwks.persistence.kubernetes.enabled")
public class KubernetesConfig {

	@Bean
	@SuppressWarnings("unused")
	ApiClient apiClient() throws IOException {
		return Config.defaultClient();
	}

	@Bean
	@SuppressWarnings("unused")
	CoreV1Api coreV1Api(ApiClient apiClient) {
		return new CoreV1Api(apiClient);
	}
}
