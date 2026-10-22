# Deploy HomeBase / BTL_CSDL on a VPS

This deployment runs the Spring Boot app, MySQL, Redis, MinIO, and Caddy. Caddy serves the app and avatar links over HTTPS. The database initialization file contains table definitions only, not local account or personal data.

## Requirements

- Linux VPS with Docker Engine and the Docker Compose plugin.
- A domain with two DNS A records pointing to the VPS public IP: one for APP_DOMAIN and another for MINIO_DOMAIN.
- Inbound TCP ports 80 and 443 open in the server firewall.

## First deployment

1. Clone the project repository on the VPS.
2. Copy production.env.example to .env.
3. Edit .env with your two domain names. Replace every replace-... value with a unique random value. Generate a new JWT signing key on the server with: openssl rand -base64 48. Never reuse the JWT key that was previously in source code.
4. Start all services:

   docker compose --env-file .env -f compose.server.yml up -d --build

   The first build takes longer because Java and MinIO are built from source.
5. Check status and application logs:

   docker compose --env-file .env -f compose.server.yml ps
   docker compose --env-file .env -f compose.server.yml logs -f app

6. Open https://APP_DOMAIN and register an account. This deployment starts with an empty database.

Caddy obtains and renews HTTPS certificates after DNS points to the VPS and ports 80/443 are reachable. MinIO's admin console is not exposed publicly. The app connects to MinIO internally for uploads and uses its HTTPS hostname when generating browser-accessible signed avatar links.

## Updating

Pull the latest code, then run:

   docker compose --env-file .env -f compose.server.yml up -d --build

## Data and secrets

MySQL, Redis, MinIO, and Caddy state use persistent Docker volumes. Back up MySQL and MinIO data regularly. Keep .env only on the server and never commit it.

database/server-schema.sql is schema-only. It does not migrate local records. If the database schema changes before the first server setup, refresh this export before deploying.
