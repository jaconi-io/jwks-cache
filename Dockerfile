FROM container-registry.oracle.com/graalvm/native-image-community:21 AS builder

WORKDIR /build

# Copy the source code into the image for building
COPY . /build

# Install xargs (part of findutils) which is required by Gradle.
RUN microdnf install findutils

# Build
RUN ./gradlew nativeCompile

# The deployment Image
FROM container-registry.oracle.com/os/oraclelinux:9-slim

EXPOSE 8080
EXPOSE 8081

WORKDIR /workspace

# Copy the native executable into the container
COPY --from=builder /build/build/native/nativeCompile .
# nobody
USER 65534
ENTRYPOINT ["/workspace/jwks-cache"]
