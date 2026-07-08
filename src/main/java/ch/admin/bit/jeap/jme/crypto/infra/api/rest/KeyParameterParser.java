package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.StringReader;
import java.security.interfaces.RSAPrivateKey;

class KeyParameterParser {

    @SneakyThrows
    static RSAPrivateKey parsePrivateKeyParameter(String privateKeyString) {
        PEMKeyPair pemKeyPair = parseKey(privateKeyString);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        return (RSAPrivateKey) converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
    }

    private static PEMKeyPair parseKey(String privateKeyString) throws IOException {
        if (privateKeyString.startsWith("{")) {
            privateKeyString = getPEMFromVaultKeyExportJson(privateKeyString);
        }
        PEMParser pemParser = new PEMParser(new StringReader(privateKeyString));
        return (PEMKeyPair) pemParser.readObject();
    }

    @SneakyThrows
    private static String getPEMFromVaultKeyExportJson(String vaultKeyExportJson) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(vaultKeyExportJson);
        String keyType = jsonNode.get("type").asText();
        String pemString = jsonNode.get("keys").get("1").asText();
        if (!"rsa-4096".equals(keyType) || !pemString.startsWith("-----BEGIN RSA PRIVATE KEY")) {
            throw new IllegalArgumentException("""
                    This is not a valid exported RSA-4096 key export JSON from vault. Make sure to export the key unwrapped,
                    and check if it is an RSA-4096 key.""");
        }
        return pemString;
    }
}
