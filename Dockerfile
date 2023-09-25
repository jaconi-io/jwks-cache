FROM container-registry.oracle.com/graalvm/native-image:17-ol9 AS builder

# Make sure xargs is available to gradle.
RUN microdnf install --assumeyes --nodocs findutils \
 && microdnf clean all \
 && rm -rf /var/cache/yum

COPY . .

RUN ./gradlew nativeCompile

# The deployment Image.
FROM container-registry.oracle.com/os/oraclelinux:9-slim

EXPOSE 8080
EXPOSE 8081

# Copy the native executable into the image.
COPY --from=builder /app/build/native/nativeCompile .
ENTRYPOINT ["/jwks-cache"]
