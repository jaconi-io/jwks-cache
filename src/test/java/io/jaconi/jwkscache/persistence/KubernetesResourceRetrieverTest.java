package io.jaconi.jwkscache.persistence;

import com.nimbusds.jose.util.Resource;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Header;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockitoExtension.class)
@ExtendWith(MockServerExtension.class)
class KubernetesResourceRetrieverTest {
	private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
			.addMixIn(Resource.class, ResourceMixin.class)
			.build();

	private final ClientAndServer client;
	private final URL url;

	@Mock
	CoreV1Api core;

	public KubernetesResourceRetrieverTest(ClientAndServer client) throws MalformedURLException {
		this.client = client;
		this.url = URI.create("http://localhost:%d".formatted(client.getPort())).toURL();
	}

	@BeforeEach
	public void setUp() {
		client.reset();
	}

	@Test
	void loadFromMissingCM() throws ApiException, IOException {
		var request = mock(CoreV1Api.APIreadNamespacedConfigMapRequest.class);
		when(request.execute()).thenThrow(new ApiException(404, ""));
		when(core.readNamespacedConfigMap("my-cm", "my-ns")).thenReturn(request);

		var retriever = new KubernetesResourceRetriever(core, JSON_MAPPER, "my-ns", "my-cm");
		assertThatThrownBy(() -> retriever.load(url)).isInstanceOf(IOException.class);
	}

	@Test
	void storeToMissingCM() throws ApiException, IOException {
		var getReq = mock(CoreV1Api.APIreadNamespacedConfigMapRequest.class);
		when(getReq.execute()).thenThrow(new ApiException(404, ""));
		when(core.readNamespacedConfigMap("my-cm", "my-ns")).thenReturn(getReq);

		var cmCaptor = ArgumentCaptor.forClass(V1ConfigMap.class);
		when(core.createNamespacedConfigMap(eq("my-ns"), cmCaptor.capture())).thenAnswer(invocation -> {
			var request = mock(CoreV1Api.APIcreateNamespacedConfigMapRequest.class);
			when(request.execute()).thenReturn(invocation.getArgument(1));
			return request;
		});

		var retriever = new KubernetesResourceRetriever(core, JSON_MAPPER, "my-ns", "my-cm");
		retriever.store(URI.create("http://example.com").toURL(), new Resource("\"foo\"", null));

		var cm = cmCaptor.getValue();
		assertThat(cm.getData()).containsExactly(
				entry("89dce6a446a69d6b9bdc01ac75251e4c322bcdff.json", "{\"content\":\"foo\",\"contentType\":null}")
		);
	}

	@Test
	void retrieveFromMissingCM() throws ApiException, IOException {
		var getReq = mock(CoreV1Api.APIreadNamespacedConfigMapRequest.class);
		when(getReq.execute()).thenThrow(new ApiException(404, ""));
		when(core.readNamespacedConfigMap("my-cm", "my-ns")).thenReturn(getReq);

		when(core.createNamespacedConfigMap(eq("my-ns"), any(V1ConfigMap.class))).thenAnswer(invocation -> {
			var request = mock(CoreV1Api.APIcreateNamespacedConfigMapRequest.class);
			when(request.execute()).thenReturn(invocation.getArgument(1));
			return request;
		});

		client.when(request().withMethod("GET"))
				.respond(response().withHeader(Header.header("Content-Type", "foo")).withBody("{}"));

		var retriever = new KubernetesResourceRetriever(core, JSON_MAPPER, "my-ns", "my-cm");
		Resource resource = retriever.retrieveResource(url);
		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("foo");
	}

	@Test
	void retrieve404() throws ApiException, IOException {
		var request = mock(CoreV1Api.APIreadNamespacedConfigMapRequest.class);
		when(request.execute()).thenThrow(new ApiException(404, ""));
		when(core.readNamespacedConfigMap("my-cm", "my-ns")).thenReturn(request);

		client.when(request().withMethod("GET"))
				.respond(response().withStatusCode(404));

		var retriever = new KubernetesResourceRetriever(core, JSON_MAPPER, "my-ns", "my-cm");
		assertThatThrownBy(() -> retriever.retrieveResource(url)).isInstanceOf(IOException.class);
	}

	@Test
	void retrieve() throws ApiException, IOException {
		var getReq = mock(CoreV1Api.APIreadNamespacedConfigMapRequest.class);
		when(getReq.execute()).thenThrow(new ApiException(404, ""));
		when(core.readNamespacedConfigMap("my-cm", "my-ns")).thenReturn(getReq);

		var cmCaptor = ArgumentCaptor.forClass(V1ConfigMap.class);
		when(core.createNamespacedConfigMap(eq("my-ns"), cmCaptor.capture())).thenAnswer(invocation -> {
			var request = mock(CoreV1Api.APIcreateNamespacedConfigMapRequest.class);
			when(request.execute()).thenReturn(invocation.getArgument(1));
			return request;
		});

		client.when(request().withMethod("GET"))
				.respond(response().withHeader(Header.header("Content-Type", "foo")).withBody("{}"));

		var retriever = new KubernetesResourceRetriever(core, JSON_MAPPER, "my-ns", "my-cm");
		Resource resource = retriever.retrieveResource(url);
		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("foo");

		getReq = mock(CoreV1Api.APIreadNamespacedConfigMapRequest.class);
		when(getReq.execute()).thenReturn(cmCaptor.getValue());
		when(core.readNamespacedConfigMap("my-cm", "my-ns")).thenReturn(getReq);

		client.reset();
		client.when(request().withMethod("GET"))
				.respond(response().withStatusCode(504));

		retriever = new KubernetesResourceRetriever(core, JSON_MAPPER, "my-ns", "my-cm");
		resource = retriever.retrieveResource(url);
		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("foo");
	}
}
