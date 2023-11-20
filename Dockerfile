FROM bellsoft/liberica-native-image-kit-container:jdk-17-nik-22-musl AS builder

WORKDIR /build

# Copy the source code into the image for building
COPY . /build

# Build
RUN ./gradlew nativeCompile

# The deployment Image
FROM bellsoft/alpaquita-linux-base:stream-musl

EXPOSE 8080
EXPOSE 8081

WORKDIR /workspace

# Copy the native executable into the container
COPY --from=builder /build/build/native/nativeCompile .
USER 65534 # nobody
ENTRYPOINT ["/workspace/jwks-cache"]