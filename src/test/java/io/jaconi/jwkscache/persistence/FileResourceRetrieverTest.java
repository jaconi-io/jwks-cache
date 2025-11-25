package io.jaconi.jwkscache.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.Header;

import com.nimbusds.jose.util.Resource;

import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockServerExtension.class)
class FileResourceRetrieverTest {
	private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
			.addMixIn(Resource.class, ResourceMixin.class)
			.build();

	private final ClientAndServer client;
	private final URL url;

	public FileResourceRetrieverTest(ClientAndServer client) throws MalformedURLException {
		this.client = client;
		this.url = URI.create("http://localhost:%d".formatted(client.getPort())).toURL();
	}

	@BeforeEach
	public void setUp() {
		client.reset();
	}

	@Test
	void loadFromMissingFolder() {
		var retriever = new FileResourceRetriever(JSON_MAPPER, new File("/does/not/exist"));
		assertThatThrownBy(() -> retriever.load(url)).isInstanceOf(FileNotFoundException.class);
	}

	@Test
	void storeToMissingFolder() {
		var retriever = new FileResourceRetriever(JSON_MAPPER, new File("/does/not/exist"));
		assertThatThrownBy(() -> retriever.store(url, new Resource("", null))).isInstanceOf(
				FileNotFoundException.class);
	}

	@Test
	void retrieveFromMissingFolder() throws IOException {
		client.when(request().withMethod("GET"))
				.respond(response().withHeader(Header.header("Content-Type", "foo")).withBody("{}"));

		var retriever = new FileResourceRetriever(JSON_MAPPER, new File("/does/not/exist"));
		Resource resource = retriever.retrieveResource(url);
		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("foo");
	}

	@Test
	void retrieve404() {
		client.when(request().withMethod("GET"))
				.respond(response().withStatusCode(404));

		var retriever = new FileResourceRetriever(JSON_MAPPER, new File("/does/not/exist"));
		assertThatThrownBy(() -> retriever.retrieveResource(url)).isInstanceOf(IOException.class);
	}

	@Test
	void retrieve() throws IOException {
		client.when(request().withMethod("GET"))
				.respond(response().withHeader(Header.header("Content-Type", "foo")).withBody("{}"));

		var retriever = new FileResourceRetriever(JSON_MAPPER, new File("/tmp"));
		Resource resource = retriever.retrieveResource(url);
		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("foo");

		client.reset();
		client.when(request().withMethod("GET"))
				.respond(response().withStatusCode(504));

		retriever = new FileResourceRetriever(JSON_MAPPER, new File("/tmp"));
		resource = retriever.retrieveResource(url);
		assertThat(resource.getContent()).isEqualTo("{}");
		assertThat(resource.getContentType()).isEqualTo("foo");
	}
}
