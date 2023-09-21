package io.jaconi.jwkscache.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Resource;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link PersistingResourceRetriever} using the filesystem as a storage backend.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("jaconi.jwks.persistence.file.enabled")
public class FileResourceRetriever extends PersistingResourceRetriever {
	private final ObjectMapper mapper;

	@Value("${jaconi.jwks.persistence.file.path}")
	private final File path;

	@Override
	protected void store(URL url, Resource resource) throws IOException {
		if (!path.exists()) {
			var success = path.mkdirs();
			if (!success) {
				throw new FileNotFoundException(path.toString());
			}
		}

		mapper.writeValue(getFile(url), resource);
	}

	@Override
	protected Resource load(URL url) throws IOException {
		var location = getFile(url);
		if (!location.exists()) {
			throw new FileNotFoundException(location.toString());
		}

		return mapper.readValue(location, Resource.class);
	}

	/**
	 * Get the {@link File} used to store the {@link Resource} retrieved from a {@link URL}.
	 *
	 * @param url the {@link URL}
	 * @return the {@link File}
	 */
	@SneakyThrows
	private File getFile(URL url) {
		var md = MessageDigest.getInstance("SHA-1");
		var hash = md.digest(url.toString().getBytes(StandardCharsets.UTF_8));
		return new File(path, HexFormat.of().formatHex(hash) + ".json");
	}
}
