# jwks-cache

Cache [JSON Web Key Sets (JWKSs)][1] downloaded from an identity provider (such as Keycloak).

## Motivation

JWKSs allow services to validate JSON Web Tokens (JWTs) without making requests to the issuer of the token. Only the
JWKS must be available. However, if the service restarts, or a key is rotated, the service must contact the issuer again
to retrieve the JWKS. If the issuer is unavailable, the service cannot validate tokens.

The problem is made worse by naive implementations of the JWKS retrieval mechanism present in many frameworks.

jwks-cache attempts to mitigate common pitfalls by using the excellent [Nimbus JOSE + JWT][2] library for features
like:

* Rate limiting
* Caching with refresh-ahead
* Retrial
* Outage tolerance

Additional to these features, we offer persistence of cached JWKSs to ensure a service safely can restart, even if the
issuer is unavailable.

## Configuration

jwks-cache is a Spring Boot project, so all regular Spring Boot configuration can be applied. Additionally, we provide
these settings:

| Parameter                       | Default                 | Description                            | Example                                         |
|---------------------------------|-------------------------|----------------------------------------|-------------------------------------------------|
| `jaconi.jwks.urls`              | `[]`                    | The JWKS URLs to download and expose   | `["https://example.com/.well-known/jwks.json"]` |
| `jwks.persistence.file.enabled` | `false`                 | Cache a JWKS as file                   | `true`                                          |
| `jwks.persistence.file.path`    | `/var/cache/jwks-cache` | The storage location for a cached JWKS | `/mnt/volume/cache`                             |

When configuring consuming services, simply move the existing JWKS URL to `jaconi.jwks.urls` and configure the
jwks-cache instead: [:8080/.well-known/jwks.json](http://localhost:8080/.well-known/jwks.json).

## Observability

### Kubernetes Probes

A liveness endpoint is available at [:8080/livez](http://localhost:8080/livez). A readiness endpoint is available at
[:8080/readyz](http://localhost:8080/readyz).

### Logging

JSON logging can be enabled by using the Spring profile `json-logging`. See [Adding Active Profiles][3] for details.

### Metrics

[Prometheus][4] metrics are exposed at [:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus) by
default.

A custom metric is available to monitor the JWKS endpoints: `jwks_cache_endpoints`. The value is `0` for unhealthy, and
`1` for healthy endpoints. The endpoints URL is in the `url` tag.

## Native Image

As long as GitHub does not provide ARM runners we manually build our multi-arch images on an ARM host:

```
VERSION="2.6.0"
./gradlew bootBuildImage
docker tag ghcr.io/jaconi-io/jwks-cache:latest ghcr.io/jaconi-io/jwks-cache:latest-arm
docker pull ghcr.io/jaconi-io/jwks-cache:$VERSION
docker buildx imagetools create \
  --tag ghcr.io/jaconi-io/jwks-cache:2.6.0-multi-arch-test \
  ghcr.io/jaconi-io/jwks-cache:$VERSION \
  ghcr.io/jaconi-io/jwks-cache:$VERSION-arm
```

[1]: <https://datatracker.ietf.org/doc/html/rfc7517#section-5> "RFC 7571"
[2]: <https://connect2id.com/products/nimbus-jose-jwt> "Nimbus JOSE + JWT"
[3]: <https://docs.spring.io/spring-boot/docs/3.1.3/reference/html/features.html#features.profiles.adding-active-profiles> "Adding Active Profiles"
[4]: <https://prometheus.io> "Prometheus"
