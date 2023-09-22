package io.jaconi.jwkscache.persistence;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Resource;

class FileResourceRetrieverTest {
	private static final URL URL;

	static {
		try {
			URL = new URL("https://example.com");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void loadNotExisting() {
		var retriever = new FileResourceRetriever(new ObjectMapper(), new File("/does/not/exist"));
		assertThatThrownBy(() -> retriever.load(URL)).isInstanceOf(FileNotFoundException.class);
	}

	@Test
	void storeNotExisting() {
		var retriever = new FileResourceRetriever(new ObjectMapper(), new File("/does/not/exist"));
		assertThatThrownBy(() -> retriever.store(URL, new Resource("", null))).isInstanceOf(
				FileNotFoundException.class);
	}
}
