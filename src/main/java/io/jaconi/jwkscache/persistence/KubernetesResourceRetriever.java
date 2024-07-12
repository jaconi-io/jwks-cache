package io.jaconi.jwkscache.persistence;

import java.io.IOException;
import java.net.URL;

import org.springframework.boot.autoconfigure.condition.ConditionalOnCloudPlatform;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cloud.CloudPlatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Resource;

import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Namespaces;

@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
@ConditionalOnProperty("jaconi.jwks.persistence.kubernetes.enabled")
public class KubernetesResourceRetriever extends PersistingResourceRetriever {
	private final CoreV1Api core;
	private final ObjectMapper objectMapper;
	private final String namespace;

	public KubernetesResourceRetriever(ObjectMapper objectMapper) throws IOException {
		var client = Config.defaultClient();
		this.core = new CoreV1Api(client);
		this.objectMapper = objectMapper;
		this.namespace = Namespaces.getPodNamespace();

		core.get
	}

	@Override
	protected void store(URL url, Resource resource) throws IOException {
		var cm = new V1ConfigMap()
				.putDataItem(url.toString())
		core.createNamespacedConfigMap(namespace);
	}

	@Override
	protected Resource load(URL url) throws IOException {
		return null;
	}
}
