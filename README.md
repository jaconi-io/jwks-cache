# jwks-cache

[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/jaconi-io/jwks-cache?label=Image&style=for-the-badge)](https://github.com/jaconi-io/jwks-cache/pkgs/container/jwks-cache)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/jaconi-io/jwks-cache/ci.yaml?branch=main&style=for-the-badge)](https://github.com/jaconi-io/jwks-cache/actions/workflows/ci.yaml)
[![Codecov](https://img.shields.io/codecov/c/github/jaconi-io/jwks-cache?style=for-the-badge)](https://codecov.io/gh/jaconi-io/jwks-cache)

[![GitHub license](https://img.shields.io/github/license/jaconi-io/jwks-cache?style=for-the-badge)](https://github.com/jaconi-io/jwks-cache/blob/main/LICENSE)
[![Conventional Commits](https://img.shields.io/badge/Conventional%20Commits-1.0.0-%23FE5196?logo=conventionalcommits&logoColor=white&style=for-the-badge)](https://conventionalcommits.org)
[![semantic-release: angular](https://img.shields.io/badge/semantic--release-angular-e10079?logo=semantic-release&style=for-the-badge)](https://github.com/semantic-release/semantic-release)

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

Additional to these features, we offer persistence of cached JWKSs to ensure a service can safely restart, even if the
issuer is unavailable.

## Configuration

jwks-cache is a Spring Boot project, so all regular Spring Boot configuration can be applied. Additionally, we provide
these settings:

| Parameter                              | Default                 | Description                            | Example                                         |
|----------------------------------------|-------------------------|----------------------------------------|-------------------------------------------------|
| `jaconi.jwks.urls`                     | `[]`                    | The JWKS URLs to download and expose   | `["https://example.com/.well-known/jwks.json"]` |
| `jaconi.jwks.persistence.file.enabled` | `false`                 | Cache a JWKS as file                   | `true`                                          |
| `jaconi.jwks.persistence.file.path`    | `/var/cache/jwks-cache` | The storage location for a cached JWKS | `/mnt/volume/cache`                             |

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

[1]: <https://datatracker.ietf.org/doc/html/rfc7517#section-5> "RFC 7571"
[2]: <https://connect2id.com/products/nimbus-jose-jwt> "Nimbus JOSE + JWT"
[3]: <https://docs.spring.io/spring-boot/docs/3.1.3/reference/html/features.html#features.profiles.adding-active-profiles> "Adding Active Profiles"
[4]: <https://prometheus.io> "Prometheus"
