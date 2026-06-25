package io.jaconi.jwkscache.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.utility.DockerImageName;

import com.nimbusds.jose.util.Resource;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Config;

@SpringBootTest
@Testcontainers
@ExtendWith(MockServerExtension.class)
@DisabledInAotMode
@TestPropertySource(properties = {
		"jaconi.jwks.persistence.kubernetes.enabled: true",
		"jaconi.jwks.persistence.kubernetes.namespace: default",
		"jaconi.jwks.persistence.kubernetes.name: jwks-cache-persistence"
})
class KubernetesResourceRetrieverTest {
	@Container
	static final K3sContainer K3S = new K3sContainer(DockerImageName.parse("rancher/k3s:v1.36.1-k3s1"));

	private static final String NAME = "jwks-cache-persistence";
	private static final String NAMESPACE = "default";

	private final ClientAndServer client;
	private final URL url;

	@TestBean
	private ApiClient apiClient;

	@Autowired
	private CoreV1Api core;

	@Autowired
	private KubernetesResourceRetriever retriever;

	KubernetesResourceRetrieverTest(ClientAndServer client) throws IOException {
		this.client = client;
		this.url = URI.create("http://localhost:%d".formatted(client.getPort())).toURL();
	}

	@BeforeEach
	void setUp() throws ApiException {
		client.reset();

		try {
			core.deleteNamespacedConfigMap(NAME, NAMESPACE).execute();
		} catch (ApiException e) {
			if (e.getCode() != HttpStatus.NOT_FOUND.value()) {
				throw e;
			}
		}
	}

	@Test
	void fallbackForNullNamespace() {
		assertThatThrownBy(() -> new KubernetesResourceRetriever(core, null, null, "unused"))
				.isInstanceOf(NoSuchFileException.class)
				.hasMessage("/var/run/secrets/kubernetes.io/serviceaccount/namespace");
	}

	@Test
	void loadFromMissingCM() {
		assertThatThrownBy(() -> retriever.load(url)).isInstanceOf(IOException.class);
	}

	@Test
	void loadFromEmptyCM() throws ApiException {
		var existing = new V1ConfigMap().metadata(new V1ObjectMeta().name(NAME).namespace(NAMESPACE));
		core.createNamespacedConfigMap(NAMESPACE, existing).execute();

		assertThatThrownBy(() -> retriever.load(url)).isInstanceOf(IOException.class);
	}

	@Test
	void storeToMissingCM() throws ApiException, IOException {
		retriever.store(url, new Resource("{\"keys\":[]}", "application/json"));

		var cm = core.readNamespacedConfigMap(NAME, NAMESPACE).execute();
		assertThat(cm.getData()).hasSize(1);
		assertThat(cm.getData().values()).containsExactly(
				"{\"content\":{\"keys\":[]},\"contentType\":\"application/json\"}"
		);
	}

	@Test
	void storeToEmptyCM() throws ApiException, IOException {
		var existing = new V1ConfigMap().metadata(new V1ObjectMeta().name(NAME).namespace(NAMESPACE));
		core.createNamespacedConfigMap(NAMESPACE, existing).execute();

		retriever.store(url, new Resource("{\"keys\":[]}", "application/json"));

		var cm = core.readNamespacedConfigMap(NAME, NAMESPACE).execute();
		assertThat(cm.getData()).hasSize(1);
		assertThat(cm.getData().values()).containsExactly(
				"{\"content\":{\"keys\":[]},\"contentType\":\"application/json\"}"
		);
	}

	@Test
	void storeToCMWithData() throws ApiException, IOException {
		var meta = new V1ObjectMeta().name(NAME).namespace(NAMESPACE);
		var existing = new V1ConfigMap().metadata(meta).data(Map.of("foo", "bar"));
		core.createNamespacedConfigMap(NAMESPACE, existing).execute();

		retriever.store(url, new Resource("{\"keys\":[]}", "application/json"));

		var cm = core.readNamespacedConfigMap(NAME, NAMESPACE).execute();
		assertThat(cm.getData()).hasSize(2);
		assertThat(cm.getData().values()).containsExactlyInAnyOrder(
				"bar", "{\"content\":{\"keys\":[]},\"contentType\":\"application/json\"}"
		);
	}

	@Test
	void retrieveStoresResourceInConfigMapAndLoadsItWhenUpstreamFails() throws IOException, ApiException {
		client.when(request().withMethod("GET")).respond(response()
				.withHeader(Header.header("Content-Type", "application/json"))
				.withBody("{}")
		);

		var resource = retriever.retrieveResource(url);

		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("application/json");

		var cm = core.readNamespacedConfigMap(NAME, NAMESPACE).execute();
		assertThat(cm.getData()).hasSize(1);
		assertThat(cm.getData().values()).containsExactly(
				"{\"content\":{},\"contentType\":\"application/json\"}"
		);

		client.reset();
		client.when(request().withMethod("GET")).respond(response().withStatusCode(504));

		var cached = retriever.retrieveResource(url);
		assertThat(cached.getContent()).isEqualTo("{}");
		assertThat(cached.getContentType()).isEqualTo("application/json");
	}

	@SuppressWarnings("unused")
	static ApiClient apiClient() throws IOException {
		return Config.fromConfig(new StringReader(K3S.getKubeConfigYaml()));
	}
}
