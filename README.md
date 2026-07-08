# JME Crypto Example

## Installing / Getting started

## Local

### Docker

Starting PostgreSQL, Minio and Vault with docker-compose up -d

### Minio

Console: [http://127.0.0.1:9001](http://127.0.0.1:9001 )
API-Port: [http://127.0.0.1:9000](http://127.0.0.1:9000 )

UserName: minioadmin
Password: minioadmin

Needs at least one bucket named 'bucket-to-use-for-connection-check'. You can create that bucket in the console
([http://127.0.0.1:9001](http://127.0.0.1:9001 ))

### Vault

Console: [http://localhost:8200/ui](http://localhost:8200/ui)

Token: secret

### Swagger

- [http://localhost:8080/jme-crypto-service/swagger-ui.html](http://localhost:8080/jme-crypto-service/swagger-ui.html)

## DEV (AWS)

### Swagger

- [https://jme-dev.nivel.bazg.admin.ch/jme-crypto-service/swagger-ui/index.html?urls.primaryName=JME%20Crypto%20Example%20Rest%20API](https://jme-dev.nivel.bazg.admin.ch/jme-crypto-service/swagger-ui/index.html?urls.primaryName=JME%20Crypto%20Example%20Rest%20API)

### S3 Bucket

- [https://eu-central-2.console.aws.amazon.com/s3/buckets/bit-jme-crypto-service-nivel-dev?region=eu-central-2&bucketType=general&tab=objects](https://eu-central-2.console.aws.amazon.com/s3/buckets/bit-jme-crypto-service-nivel-dev?region=eu-central-2&bucketType=general&tab=objects)

### KMS Key

- [https://eu-central-2.console.aws.amazon.com/kms/home?region=eu-central-2#/kms/keys/64a6f716-2a16-4cd5-b5fd-24e57fdc0b26](https://eu-central-2.console.aws.amazon.com/kms/home?region=eu-central-2#/kms/keys/64a6f716-2a16-4cd5-b5fd-24e57fdc0b26)

## Escrow Decryption

The [API](https://dev-jme-internal.bit.admin.ch/jme-crypto-service/swagger-ui) contains an endpoint that demonstrates
escrow decryption. To use it, provide the private key of the escrow key as request body, and a jEAP crypto container
encrypted using AWS KMS as the input. The escrow decryption will then
be applied using the escrow key, and the plaintext will be returned.

## DEV (AWS with on prem Vault)

### Swagger

- [https://jme-dev.nivel.bazg.admin.ch/jme-crypto-vault-service/swagger-ui/index.html?urls.primaryName=JME%20Crypto%20Example%20Rest%20API](https://jme-dev.nivel.bazg.admin.ch/jme-crypto-service/swagger-ui/index.html?urls.primaryName=JME%20Crypto%20Example%20Rest%20API)

### S3 Bucket

- [https://eu-central-2.console.aws.amazon.com/s3/buckets/bit-jme-crypto-vault-service-nivel-dev?region=eu-central-2&bucketType=general&tab=objects](https://eu-central-2.console.aws.amazon.com/s3/buckets/bit-jme-crypto-service-nivel-dev?region=eu-central-2&bucketType=general&tab=objects)

## DEV (RHOS)

### Swagger

- [https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-crypto-service/swagger-ui.html](https://bit-jme-d.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch/jme-crypto-service/swagger-ui.html)

### Notes to MAV
We are using the MAV in this example, on MAV there can only be one service account that is authorized to access the MAV, so we take the default
MAV is the Managed Vault on RHOS.
The default service account exist in every namespace, normally we use dedicated service accounts for each microservice.

## Maven Profiles

Since this example is deployed twice on AWS with different configurations, there are two Maven profiles.
One for the implementation on AWS with the on-prem Vault and one for the KMS/other platforms.
The KMS profile (`non-aws-vault`) is the default profile and is used when the `aws-vault` profile is NOT specified.
The `aws-vault` is used when the Vault profile it is specified by the Spring active profiles.

Depending on the active Maven profile, the values for 'topic' and 'appName' in the annotations are adjusted for the
message contracts.
This is necessary because only one message contract per event can exist for each encryption key.
The maven-antrun-plugin is used to modify the source code files before compilation.

So for the `aws-vault` profile, a different topic and app name are set compared to the default example configuration.
Additionally, the profiles differ in which ECR they are pushed to on AWS.

## Documentation

Check the [jEAP Blueprint Microservice](https://confluence.bit.admin.ch/display/JEAP/Blueprint+Microservices) for
further documentation

