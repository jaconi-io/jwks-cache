# jwks-cache

Cache JWKS downloaded from an identity provider (such as Keycloak).

## Observability

### Kubernetes Probes

A liveness endpoint is available at [:8080/livez](http://localhost:8080/livez). A readiness endpoint is available at
[:8080/readyz](http://localhost:8080/readyz).

### Logging

JSON logging can be enabled by using the Spring profile `json-logging`. See [Adding Active Profiles][1] for details.

### Metrics

[Prometheus][2] metrics are exposed at [:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus) by
default.

A custom metric is available to monitor the JWKS endpoints: `jwks_cache_endpoints`. The value is `0` for unhealthy, and
`1` for healthy endpoints. The endpoints URL is in the `url` tag.

[1]: <https://docs.spring.io/spring-boot/docs/3.1.3/reference/html/features.html#features.profiles.adding-active-profiles> "Adding Active Profiles"
[2]: <https://prometheus.io> "Prometheus"
