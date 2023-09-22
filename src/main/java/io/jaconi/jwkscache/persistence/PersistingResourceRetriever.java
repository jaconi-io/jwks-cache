package io.jaconi.jwkscache.persistence;

import static com.nimbusds.jose.jwk.source.JWKSourceBuilder.DEFAULT_HTTP_CONNECT_TIMEOUT;
import static com.nimbusds.jose.jwk.source.JWKSourceBuilder.DEFAULT_HTTP_READ_TIMEOUT;
import static com.nimbusds.jose.jwk.source.JWKSourceBuilder.DEFAULT_HTTP_SIZE_LIMIT;

import java.io.IOException;
import java.net.URL;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link ResourceRetriever} that delegates to a {@link DefaultResourceRetriever}. The {@link Resource} instances
 * retrieved from the {@link DefaultResourceRetriever} are stored by the implementing classes. If the retrieval from the
 * {@link DefaultResourceRetriever} fails, a previously stored {@link Resource} is loaded.
 */
@Slf4j
abstract class PersistingResourceRetriever implements ResourceRetriever {
	private final ResourceRetriever delegate = new DefaultResourceRetriever(
			DEFAULT_HTTP_CONNECT_TIMEOUT,
			DEFAULT_HTTP_READ_TIMEOUT,
			DEFAULT_HTTP_SIZE_LIMIT);

	@Override
	public Resource retrieveResource(URL url) throws IOException {
		Resource resource;
		try {
			resource = delegate.retrieveResource(url);
		} catch (IOException e) {
			// Resource retrieval failed. Try to load and return a previously stored resource.
			try {
				return load(url);
			} catch (IOException e1) {
				// Stored resource could not be loaded. Log a message, but throw the original exception.
				log.warn("failed to load persisted JWKS", e1);
				throw e;
			}
		}

		// Store the successfully retrieved resource for future use.
		try {
			store(url, resource);
		} catch (IOException e) {
			log.error("failed to persist JWKS", e);
		}

		return resource;
	}

	/**
	 * Store the {@link Resource} retrieved from the {@link URL}.
	 *
	 * @param url      the {@link URL}
	 * @param resource the {@link Resource}
	 * @throws IOException if an error occurs while storing the resource
	 */
	protected abstract void store(URL url, Resource resource) throws IOException;

	/**
	 * Load a {@link Resource} that was previously retrieved from the {@link URL}.
	 *
	 * @param url the {@link URL}
	 * @return the {@link Resource}
	 * @throws IOException if no resource is available or an error occurs while loading the resource
	 */
	protected abstract Resource load(URL url) throws IOException;
}
