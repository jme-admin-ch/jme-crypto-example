#!/bin/sh

# Login on RHOS with token (oc login ....)

export VAULT_ADDR=https://vault-bfdcbaf9-e8b9-47c7-b296-5b0b0381aeb8.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch

# login with token
vault login [Token see Margun Keepass]

vault secrets enable -path=transit/jme transit

vault write -f transit/jme/keys/jme-crypto-example-s3-key
vault write -f transit/jme/keys/jme-crypto-example-database-key
vault write -f transit/jme/keys/jme-crypto-example-messaging-key

# Put a test secret into the secrets engine at /bit-jme-d
 vault kv put bit-jme-d/jme-crypto-service test.testSecret=vault-secret-value

# Enable JWT auth method
vault auth enable -path=crypto jwt

# Write JWKS url to the new Auth Method
vault write auth/crypto/config \
  jwks_url=$(oc whoami --show-server)/openid/v1/jwks \
  jwks_ca_pem=@assets/SwissGovernmentEIntra01.crt \
  bound_issuer="https://kubernetes.default.svc"
vault read auth/crypto/config

# Create policy 'jme-crypto-service-policy' for the approle with path restriction
vault policy write jme-crypto-service-policy jme-cryptop-service-vault-pol.hcl


# Create new Auth-Role
vault write auth/crypto/role/crypto-role \
   role_type="jwt" \
   bound_audiences="https://kubernetes.default.svc" \
   user_claim="/kubernetes.io/pod/name" \
   user_claim_json_pointer=true \
   bound_subject="system:serviceaccount:bit-jme-d:jme-crypto-service-runtime-sa" \
   token_ttl="10m" \
   token_explicit_max_ttl="10m" \
   token_policies="jme-crypto-service-policy"






