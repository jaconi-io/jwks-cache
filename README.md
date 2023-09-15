# jwks-cache

Cache JWKS downloaded from an identity provider (such as Keycloak).

## Observability

### Kubernetes Probes

A liveness endpoint is available at [:8080/livez](http://localhost:8080/livez). A readiness endpoint is available at
[:8080/readyz](http://localhost:8080/readyz).

### Logging

JSON logging can be enabled by using the Spring profile `json-logging`. See [Adding Active Profiles][1] for details.

### Metrics

[Prometheus][2] metrics are exposed at
[:8081/actuator/prometheus](http://localhost:8081/actuator/prometheus) by default.

## Testing

We use [Docker Compose](https://docs.docker.com/compose/) to create a test environment.

First, build the applications using gradle:

```shell
./gradlew bootBuildImage micronaut-test:dockerBuild
```

Then, run `docker compose`:

```shell
docker compose up --detach
```

Get an access token from [dex](https://dexidp.io):

```shell
token=$(./access_token.php)
```

Make sure to grant access in your browser.

Request to the application:

```shell
curl --write-out '%{http_code}' --header "Authorization: Bearer $token" http://localhost:8080/test
```

It should return a 204.

Stop dex:

```
docker compose stop dex
```

Request to the application again:

```shell
curl --write-out '%{http_code}' --header "Authorization: Bearer $token" http://localhost:8080/test
```

It will still return a 204.

Perform a request with an invalid token:

```shell
curl --write-out '%{http_code}' --header "Authorization: Bearer $(echo $token | sed 's/.$//')" http://localhost:8080/test
```

Perform another request with a valid token:

```shell
curl --write-out '%{http_code}' --header "Authorization: Bearer $token" http://localhost:8080/test
```

A 204 will be returned (without jwks-cache a 401 would have been returned).

[1]: <https://docs.spring.io/spring-boot/docs/3.1.3/reference/html/features.html#features.profiles.adding-active-profiles> "Adding Active Profiles"
[2]: <https://prometheus.io> "Prometheus"
