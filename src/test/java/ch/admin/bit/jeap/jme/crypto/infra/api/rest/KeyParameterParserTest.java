package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import org.junit.jupiter.api.Test;

import java.security.interfaces.RSAPrivateKey;

import static org.assertj.core.api.Assertions.assertThat;

class KeyParameterParserTest {

    @Test
    void parsePrivateKeyParameter_jsonExportFromVault() {
        String json = """
                {
                  "keys": {
                    "1": "%s"
                  },
                  "type": "rsa-4096",
                  "name": "random-test-key"
                }
                """.formatted(TEST_KEY.replace("\n", "\\n"));

        RSAPrivateKey rsaPrivateKey = KeyParameterParser.parsePrivateKeyParameter(json);

        assertThat(rsaPrivateKey.getAlgorithm())
                .isEqualTo("RSA");
    }

    @Test
    void parsePrivateKeyParameter() {
        RSAPrivateKey rsaPrivateKey = KeyParameterParser.parsePrivateKeyParameter(TEST_KEY);

        assertThat(rsaPrivateKey.getAlgorithm())
                .isEqualTo("RSA");
    }

    // This is a random RSA-4096 test key generated and exported from vault
    private final static String TEST_KEY = """
            -----BEGIN RSA PRIVATE KEY-----
            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
            -----END RSA PRIVATE KEY-----
            """;
}
