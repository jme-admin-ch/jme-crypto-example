# JME Crypto Example

This example project shows how to use the [jeap-crypto](https://github.com/jeap-admin-ch/jeap-crypto) library to
encrypt and decrypt data with keys managed by HashiCorp Vault, including an escrow decryption path and an
S3-backed object store scenario.

## What this example demonstrates

The application simulates a small "video game store" and uses encryption in three different ways:

- **Direct encrypt/decrypt roundtrip** (`CryptoRestController`, `/api/crypto`) — encrypts and decrypts arbitrary
  plaintext using a `KeyReferenceCryptoService` and a key location configured via `jme.crypto.test.keylocation`.
  Useful as the simplest possible starting point to understand the library's API.
- **Encrypted entity persisted to PostgreSQL** (`GamesRestController`, `/api/games`) — creates a `Game` whose `name`
  is encrypted before being stored via JPA/Flyway, and transparently decrypted when read back
  (`GameService`/`GameRepository`).
- **Encrypted content persisted to S3** (`GameReviewsRestController`, `/api/gamereviews`) — creates a `GameReview`
  whose `reviewText` is encrypted and stored in an S3-compatible object store (`S3ObjectStoreRepository`, backed
  by MinIO locally), and publishes a Kafka event (`GameReviewPublisher`/`KafkaGameReviewPublisher`) when a review
  is created.
- **Escrow decryption** (`EscrowRestController`, `/api/escrow`) — decrypts a jEAP crypto data container using an
  escrow private key directly (no Vault involved), demonstrating the "break glass" recovery path. See the endpoint's
  Swagger description for the exact key format expected.

All endpoints are documented and can be tried out via Swagger UI (see below).

## Changes

This library is versioned using [Semantic Versioning](http://semver.org/) and all changes are documented in
[CHANGELOG.md](./CHANGELOG.md) following the format defined in [Keep a Changelog](http://keepachangelog.com/).

## Prerequisites

To use this project, ensure you have the following installed:

1. **Java Development Kit (JDK)**: Version 25.
2. **Docker**: For running the required infrastructure.

**Note:** Use the provided maven wrapper to build and run the project.

## Getting started

### Infrastructure

Before the examples can be started the infrastructure has to be started using docker. This starts a PostgreSQL
database, a Kafka broker with schema registry, a MinIO S3-compatible object store, and a HashiCorp Vault instance
(used for key management):

```shell
docker-compose -f docker/docker-compose.yml up
```

### Build

The project itself can be built with a simple

```shell
./mvnw install
```

### Start

Then the project can be started using

```shell
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Trying it out

Once the application is running, open the Swagger UI at
[http://localhost:8080/jme-crypto-service/swagger-ui.html](http://localhost:8080/jme-crypto-service/swagger-ui.html)
to explore and call the `/api/crypto`, `/api/games`, `/api/gamereviews` and `/api/escrow` endpoints described above.

## Profiles

* **application-local:** Contains all configurations for running the application locally, with Vault providing the
  encryption keys.
* **application-local-disable-encryption:** Contains all configurations for running the application locally without
  encryption — useful to compare plaintext vs. encrypted storage.

## Note

This repository is part of the open source distribution of JME. See [github.com/jme-admin-ch/jme](https://github.com/jme-admin-ch/jme)
for more information.

## License

This repository is Open Source Software licensed under the [Apache License 2.0](./LICENSE).
