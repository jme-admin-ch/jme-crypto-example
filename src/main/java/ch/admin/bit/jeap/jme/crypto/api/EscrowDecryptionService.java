package ch.admin.bit.jeap.jme.crypto.api;

import ch.admin.bit.jeap.crypto.api.CryptoException;
import ch.admin.bit.jeap.crypto.awskms.key.AwsKmsEncryptedDataKeyFormat;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.ByteBufferUtil;
import ch.admin.bit.jeap.crypto.internal.core.dataformat.JeapCryptoMultiKeyDataFormat;
import ch.admin.bit.jeap.crypto.internal.core.model.JeapCryptoContainer;
import com.amazon.corretto.crypto.provider.AmazonCorrettoCryptoProvider;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A service demonstrating how to decrypt data using a data key that has been encrypted using the public key of an
 * RSA-4096 escrow key.
 * <p>
 * This is mainly for demonstration purposes and to validate that escrow decryption actually works. To test, encrypt
 * some data to a jEAP crypto container on AWS, then decrypt it using this service, by providing the private key of
 * the escrow key. This will decrypt the data without contacting the Key Management Service. To do so, the data key is
 * first decrypted using the escrow key and RSA-4096 as the algorithm, then the data is decrypted using the data key and
 * AES-GCM-256 as the algorithm.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowDecryptionService {

    private static final JeapCryptoMultiKeyDataFormat DATA_FORMAT = new JeapCryptoMultiKeyDataFormat(new AwsKmsEncryptedDataKeyFormat());

    /**
     * The algorithm used for decrypting the escrow-encrypted data key
     */
    private static final String TRANSFORMATION = "RSA/ECB/OAEPPadding";
    private static final String AES = "AES";
    /**
     * The algorithm used for decrypting the data encrypted with the data key
     */
    private static final String CRYPTO_ALGO = AES + "/GCM/NoPadding";
    // 16-byte auth tag, needs to be set as parameter for AES decryption (fixed value used in the encryption algo)
    private static final int TAG_LENGTH_BYTES = 16;

    public byte[] decrypt(byte[] dataContainerBytes, RSAPrivateKey escrowPrivateKey) {
        // 1. Read the data key ciphertext, encrypted with the public key of the escrow key
        byte[] encryptedDataKey = getEncryptedDataKey(dataContainerBytes);
        SecretKey decryptedDataKey = decryptDataKey(escrowPrivateKey, encryptedDataKey);

        // 2. Parse the crypto container to extract the ciphertext
        JeapCryptoContainer jeapCryptoContainer = DATA_FORMAT.parse(dataContainerBytes);

        // 3. Decrypt the data using the data key, which has been decrypted using the escrow key
        return decryptData(decryptedDataKey, jeapCryptoContainer.nonce(), jeapCryptoContainer.ciphertext());
    }

    @SneakyThrows
    private static SecretKeySpec decryptDataKey(RSAPrivateKey key, byte[] cipherText) {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION, AmazonCorrettoCryptoProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plaintextDataKey = cipher.doFinal(cipherText);
        return new SecretKeySpec(plaintextDataKey, AES);
    }

    private byte[] getEncryptedDataKey(byte[] dataContainerBytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(dataContainerBytes);
        byteBuffer.get(); // skip data format byte, already validated when parsing the container
        byte numberOfDataKeys = byteBuffer.get();
        assertFieldValueEquals("Number of data keys", (byte) 2, numberOfDataKeys);

        // 1. Skip first key (KMS encrypted key)
        int firstKeySize = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byteBuffer.position(byteBuffer.position() + firstKeySize - 2);

        // 2. Validate and parse data key encrypted with escrow key
        ByteBufferUtil.readUnsignedShort(byteBuffer); // skip block size field
        int escrowDataKeyFormatId = ByteBufferUtil.readUnsignedShort(byteBuffer);
        assertFieldValueEquals("Escrow data key format ID", 100, escrowDataKeyFormatId);
        int keyProviderLength = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] keyProviderIdBytes = new byte[keyProviderLength];
        byteBuffer.get(keyProviderIdBytes);
        String keyProviderId = new String(keyProviderIdBytes, UTF_8);
        assertFieldValueEquals("Key provider ID", "rsa4096", keyProviderId);
        int escrowDataKeySize = ByteBufferUtil.readUnsignedShort(byteBuffer);
        byte[] encryptedEscrowDataKey = new byte[escrowDataKeySize];
        byteBuffer.get(encryptedEscrowDataKey);
        return encryptedEscrowDataKey;
    }

    private void assertFieldValueEquals(String msg, Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(msg + " (%s != %s)".formatted(expected, actual));
        }
    }

    private byte[] decryptData(SecretKey key, byte[] nonce, byte[] cipherText) {
        try {
            Cipher cipher = Cipher.getInstance(CRYPTO_ALGO, AmazonCorrettoCryptoProvider.PROVIDER_NAME);
            // Tag length parameter is in bits, hence '*8'
            GCMParameterSpec params = new GCMParameterSpec(TAG_LENGTH_BYTES * 8, nonce);
            cipher.init(Cipher.DECRYPT_MODE, key, params);
            return cipher.doFinal(cipherText);
        } catch (GeneralSecurityException e) {
            throw CryptoException.decryptionFailed(e);
        }
    }
}
