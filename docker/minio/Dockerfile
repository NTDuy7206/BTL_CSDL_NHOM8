FROM golang:1.24-alpine AS builder

ENV CGO_ENABLED=0 \
    GOBIN=/out

RUN go install github.com/minio/minio@latest

FROM alpine:3.22

RUN apk add --no-cache ca-certificates \
    && addgroup -S minio \
    && adduser -S -G minio minio \
    && mkdir -p /data \
    && chown minio:minio /data

COPY --from=builder /out/minio /usr/local/bin/minio

USER minio
EXPOSE 9000 9001
ENTRYPOINT ["minio"]
CMD ["server", "/data", "--address", ":9000", "--console-address", ":9001"]
