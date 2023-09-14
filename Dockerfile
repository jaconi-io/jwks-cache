FROM --platform=$BUILDPLATFORM golang:1.21 AS builder

WORKDIR /app

COPY go.mod go.mod
COPY go.sum go.sum
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=$TARGETOS GOARCH=$TARGETARCH go build

FROM scratch
COPY --from=builder /app/jwks-cache /jsks-cache
ENTRYPOINT [ "/jwks-cache" ]
