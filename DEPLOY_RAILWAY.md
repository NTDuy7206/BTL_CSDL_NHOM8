# Deploy this project on Railway

Railway deploys each component as its own service. This project needs four: the Spring Boot web app, Railway MySQL, Railway Redis, and MinIO. The app and MinIO use Dockerfiles in this repository. Railway provides HTTPS domains, so Caddy is not needed here.

## Before starting

- Sign in to Railway with the GitHub account that can access NTDuy7206/BTL_CSDL_NHOM8.
- The deployment files and code changes must be pushed to GitHub before Railway can build them. The current local changes are not pushed yet.
- Railway's current Hobby plan is $5/month and includes $5 of usage; usage above that is charged separately. The Free plan has $1/month of usage credit after its trial. Four long-running services can use more than the free credit, so check the usage page before enabling paid resources.

## 1. Create the project and databases

1. Create an Empty Project in Railway.
2. Add a MySQL database service. Keep it private.
3. Add a Redis database service. Keep it private.
4. Rename the services exactly MySQL and Redis, so the variable references below match.

The Spring Boot production profile creates the schema from the schema-only SQL resource at startup. It creates the core tables and workflow tables if missing and does not import the local accounts or other local records.

## 2. Add the Spring Boot service

1. Add a service from the GitHub repository NTDuy7206/BTL_CSDL_NHOM8, branch main.
2. Use the repository root. Railway will build the root Dockerfile.
3. Set these variables on the app service. The Railway reference expressions are case-sensitive; keep the service names as shown.

SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?characterEncoding=UTF-8&serverTimezone=UTC
DB_USERNAME=${{MySQL.MYSQLUSER}}
DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
REDIS_HOST=${{Redis.REDISHOST}}
REDIS_PORT=${{Redis.REDISPORT}}
REDIS_PASSWORD=${{Redis.REDISPASSWORD}}
JWT_SECRET=<generate a new random Base64 secret>
MINIO_ENDPOINT=http://${{MinIO.RAILWAY_PRIVATE_DOMAIN}}:9000
MINIO_PUBLIC_ENDPOINT=https://${{MinIO.RAILWAY_PUBLIC_DOMAIN}}
MINIO_ACCESS_KEY=${{MinIO.MINIO_ROOT_USER}}
MINIO_SECRET_KEY=${{MinIO.MINIO_ROOT_PASSWORD}}
MINIO_BUCKET=homebase-avatars

Generate JWT_SECRET on your computer; do not paste it into GitHub or this document. For example, in PowerShell:

    $bytes = New-Object byte[] 48
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
    [Convert]::ToBase64String($bytes)

Railway supplies PORT to the app automatically. Do not set a fixed public port for the app.

## 3. Add MinIO for avatar images

1. Add another service from the same GitHub repository.
2. In the service settings, set Root Directory to /docker/minio. Railway will use the Dockerfile in that folder.
3. Rename this service exactly MinIO.
4. Set variables:
   - PORT=9000
   - MINIO_ROOT_USER=<a new storage username>
   - MINIO_ROOT_PASSWORD=<a new long random password>
   - RAILWAY_RUN_UID=0
5. Attach a Railway Volume to this service with mount path /data. This keeps uploaded avatars between deploys.
6. Generate a public domain for this service and set its target port to 9000. Do not expose port 9001, which is the MinIO admin console.

After the public domain is generated, Railway's RAILWAY_PUBLIC_DOMAIN reference in the app service produces HTTPS links that browsers can open. The MINIO_ENDPOINT reference uses Railway's private network for uploads.

## 4. Publish the app

1. Wait for MySQL, Redis, MinIO, and the app deployment to finish.
2. In the app service settings, open Networking and choose Generate Domain.
3. Open the generated HTTPS domain. The first app startup creates the empty schema; then register a new account.
4. If the app fails to start, inspect its deployment logs first. Common causes are a mistyped service name in a variable reference, missing MinIO public domain, or MySQL not yet accepting connections.

Do not enable Public Networking for MySQL or Redis. Only the web app and MinIO API need public domains; avatars remain protected by short-lived signed URLs.

## Updating

Push a new commit to the connected GitHub branch. Railway will rebuild the app service. The MinIO service uses the same repository but a different root directory, so it may also rebuild when the repository changes.
