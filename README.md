# jwks-cache

Cache JWKS downloaded from an identity provider (such as Keycloak).

## Testing

We use [Docker Compose](https://docs.docker.com/compose/) to create a test environment.

First, build the applications using gradle:

```shell
./gradlew bootBuildImage
./micronaut-test/gradlew --project-dir micronaut-test dockerBuild
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
