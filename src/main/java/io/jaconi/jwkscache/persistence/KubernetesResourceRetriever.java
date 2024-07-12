package io.jaconi.jwkscache.persistence;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.util.Resource;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Namespaces;
import io.kubernetes.client.util.PatchUtils;
import io.kubernetes.client.util.Strings;
import lombok.SneakyThrows;
import tools.jackson.core.io.JsonStringEncoder;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty("jaconi.jwks.persistence.kubernetes.enabled")
public class KubernetesResourceRetriever extends PersistingResourceRetriever {
	private final CoreV1Api core;
	private final JsonMapper jsonMapper;
	private final String namespace;
	private final String name;

	public KubernetesResourceRetriever(
			CoreV1Api coreV1Api,
			JsonMapper jsonMapper,
			@Value("${jaconi.jwks.persistence.kubernetes.namespace}") String namespace,
			@Value("${jaconi.jwks.persistence.kubernetes.name}") String name
	) throws IOException {
		this.core = coreV1Api;
		this.jsonMapper = jsonMapper;
		this.name = name;

		if (Strings.isNullOrEmpty(namespace)) {
			this.namespace = Namespaces.getPodNamespace();
		} else {
			this.namespace = namespace;
		}
	}

	@Override
	protected void store(URL url, Resource resource) throws IOException {
		V1ConfigMap existing = null;
		try {
			existing = core.readNamespacedConfigMap(name, namespace).execute();
		} catch (ApiException e) {
			if (e.getCode() != HttpStatus.NOT_FOUND.value()) {
				throw new IOException("failed to get existing ConfigMap %s/%s".formatted(namespace, name), e);
			}
		}

		var key = getKey(url);
		var data = jsonMapper.writeValueAsString(resource);

		if (existing == null) {
			var cm = new V1ConfigMap()
					.metadata(new V1ObjectMeta().namespace(namespace).name(name))
					.putDataItem(key, data);

			try {
				core.createNamespacedConfigMap(namespace, cm).execute();
			} catch (ApiException e) {
				throw new IOException("failed to create ConfigMap %s/%s".formatted(namespace, name), e);
			}
		} else {
			var escapedData = new StringBuilder();
			JsonStringEncoder.getInstance().quoteAsString(data, escapedData);

			var patch = new V1Patch(
					"[{\"op\":\"add\",\"path\":\"/data/%s\",\"value\":\"%s\"}]".formatted(key, escapedData)
			);

			try {
				PatchUtils.patch(
						V1ConfigMap.class,
						() -> core.patchNamespacedConfigMap(name, namespace, patch).buildCall(null),
						V1Patch.PATCH_FORMAT_JSON_PATCH
				);
			} catch (ApiException e) {
				throw new IOException("failed to patch Kubernetes ConfigMap %s/%s".formatted(namespace, name), e);
			}
		}
	}

	@Override
	protected Resource load(URL url) throws IOException {
		V1ConfigMap cm;
		try {
			cm = core.readNamespacedConfigMap(name, namespace).execute();
		} catch (ApiException e) {
			throw new IOException("failed to get existing ConfigMap %s/%s".formatted(namespace, name), e);
		}

		if (cm.getData() == null) {
			throw new IOException("existing ConfigMap %s/%s is empty".formatted(namespace, name));
		}

		var key = getKey(url);
		var data = cm.getData().get(key);
		if (data == null) {
			throw new IOException("existing ConfigMap %s/%s does not contain %s".formatted(namespace, name, key));
		}

		return jsonMapper.readValue(data, Resource.class);
	}

	/**
	 * Get the key used to store the {@link Resource} retrieved from a {@link URL} in a Kubernetes {@link V1ConfigMap}.
	 *
	 * @param url the {@link URL}
	 * @return the key
	 */
	@SneakyThrows
	private String getKey(URL url) {
		var md = MessageDigest.getInstance("SHA-1");
		var hash = md.digest(url.toString().getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(hash) + ".json";
	}
}
