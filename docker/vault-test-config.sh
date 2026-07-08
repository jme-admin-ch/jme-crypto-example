#!/bin/sh

# Make sure vault is started
while ! nc -z vault-server 8200 ; do sleep 1 ; done

set -exo pipefail

unset http_proxy
unset HTTP_PROXY
unset http_proxy
unset https_proxy

export VAULT_ADDR=http://vault-server:8200

vault login secret

vault audit enable file file_path=stdout
vault secrets enable -path=transit/jme transit

vault write -f transit/jme/keys/jme-crypto-example-s3-key
vault write -f transit/jme/keys/jme-crypto-example-database-key
vault write -f transit/jme/keys/jme-crypto-example-messaging-key


# Put a test secret into the secrets engine at /secret
vault kv put secret/jme/jme-crypto-service test.testSecret=vault-secret-value

# Enable approle auth method
vault auth enable -path=approle/jme approle

# Create policy 'jme-crypto-service-policy' for the approle with path restriction
SCRIPT_DIR=`dirname $0`
vault policy write jme-crypto-service-policy ${SCRIPT_DIR}/jme-cryptop-service-vault-pol.hcl

# Create approle for jme-crypto-service-approle, assign the jme-crypto-service-policy
APPROLE_PATH=auth/approle/jme/role/jme-crypto-service-approle
vault write ${APPROLE_PATH} \
   bind_secret_id=true \
   token_policies=jme-crypto-service-policy

# Log approle
vault read ${APPROLE_PATH}

# Set fixed role-id for local tests
ROLE_ID=9999-8888-7777
vault write ${APPROLE_PATH}/role-id \
  role_name=jme-crypto-service-approle \
  role_id="${ROLE_ID}"

# Set fixed secret-id for local tests
SECRET_ID=1234-5678-9012-3456
vault write ${APPROLE_PATH}/custom-secret-id \
  role_name=jme-crypto-service-approle \
  secret_id="${SECRET_ID}"


# Get app-id and generate new secret-id for app-role
vault list auth/approle/jme/role
vault read auth/approle/jme/role/jme-crypto-service/role-id
#vault write -f auth/approle/jme/role/jme-crypto-service/secret-id
