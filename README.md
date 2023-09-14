# jwks-cache

Cache JWKS downloaded from an identity provider (such as Keycloak).

## Testing

We use [Docker Compose](https://docs.docker.com/compose/) to create a test environment.

First, build the Micronaut test application using gradle:

```shell
./micronaut-test/gradlew --project-dir micronaut-test dockerBuild
```

Then, run `docker compose`:

```shell
docker compose up
```

Get an access token from [dex](https://dexidp.io):

```shell
token=$(./access_token.php)
```

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

It will return a 401.
