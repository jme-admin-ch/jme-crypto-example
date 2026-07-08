package ch.admin.bit.jeap.jme.crypto.api;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKey;
import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.aes.AesGcmCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoMultiKeyDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.escrow.AsymmetricEscrowEncryptionService;
import ch.admin.bit.jeap.crypto.internal.core.escrow.EscrowKeyType;
import ch.admin.bit.jeap.crypto.internal.core.keymanagement.KeyManagementService;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.DataKeyPair;
import ch.admin.bit.jeap.crypto.internal.core.model.EncryptedDataKey;
import ch.admin.bit.jeap.crypto.internal.core.model.EscrowDataKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class EscrowDecryptionServiceTest {

    private static final JeapCryptoMultiKeyDataFormat DATA_FORMAT = new JeapCryptoMultiKeyDataFormat(new AwsKmsEncryptedDataKeyFormat());
    private final EscrowDecryptionService escrowDecryptionService = new EscrowDecryptionService();

    @Test
    void decrypt() {
        String plaintext = "Hello, World";
        KeyPair escrowKey = generateEscrowKeyPair();
        byte[] ciphertextContainer = encryptPlaintext(escrowKey, plaintext);

        byte[] decryptedPlaintextBytes = escrowDecryptionService.decrypt(ciphertextContainer, (RSAPrivateKey) escrowKey.getPrivate());

        assertThat(new String(decryptedPlaintextBytes, UTF_8))
                .isEqualTo(plaintext);
    }

    private byte[] encryptPlaintext(KeyPair escrowKey, String plaintext) {
        KeyManagementService keyManagementService = createKeyManagementService(escrowKey);
        AesGcmCryptoService cryptoService = new AesGcmCryptoService(keyManagementService, DATA_FORMAT);
        return cryptoService.encrypt(plaintext.getBytes(UTF_8), new KeyReference("test"));
    }

    private KeyManagementService createKeyManagementService(KeyPair escrowKey) {
        DataKey dataKey = generateDataKey();
        EscrowDataKey escrowDataKey = createEscrowDataKey(escrowKey, dataKey);
        byte[] kmsEncryptedKeyCiphertext = new byte[10]; // not used, can be empty
        EncryptedDataKey encryptedDataKey = new AwsKmsEncryptedDataKey(kmsEncryptedKeyCiphertext, escrowDataKey, "keyId");
        return createKeyManagementServiceStub(dataKey, encryptedDataKey);
    }

    private static EscrowDataKey createEscrowDataKey(KeyPair escrowKey, DataKey dataKey) {
        PublicKey escrowPublicKey = escrowKey.getPublic();
        AsymmetricEscrowEncryptionService escrowEncryptionService = new AsymmetricEscrowEncryptionService();
        return escrowEncryptionService.encryptEscrowDataKey(dataKey, EscrowKeyType.RSA_4096, escrowPublicKey);
    }

    private static DataKey generateDataKey() {
        byte[] plaintextDataKey = new byte[256 / 8];
        new SecureRandom().nextBytes(plaintextDataKey);
        return new DataKey(plaintextDataKey);
    }

    @SneakyThrows
    private KeyPair generateEscrowKeyPair() {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(4096, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    private KeyManagementService createKeyManagementServiceStub(DataKey dataKey, EncryptedDataKey encryptedDataKey) {
        return new KeyManagementService() {
            @Override
            public DataKeyPair getDataKey(KeyReference wrappingKeyReference) {
                return new DataKeyPair(dataKey, encryptedDataKey);
            }

            @Override
            public byte[] decryptDataKey(KeyReference wrappingKeyReference, EncryptedDataKey dataKey) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
