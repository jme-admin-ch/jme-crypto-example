package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import ch.admin.bit.jeap.crypto.api.KeyReference;
import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Tag(name = "Crypto Test Resource",
        description = "Simple encrypt/decrypt-Roundtrip. Example with KeyReferenceCryptoService")
@RestController
@RequestMapping("/api/crypto")
@Slf4j
@RequiredArgsConstructor
public class CryptoRestController {

    @Value("${jme.crypto.test.keylocation}")
    private String keyLocation;

    private final KeyReferenceCryptoService keyReferenceCryptoService;

    @Operation(summary = "Encrypt and return the Ciphertext as Base64-Encoded String. Copy that.")
    @PutMapping("/encrypt")
    public String encrypt(@RequestParam String plaintext) {
        KeyReference keyReference = new KeyReference(keyLocation);
        byte[] encryptedText = keyReferenceCryptoService.encrypt(plaintext.getBytes(UTF_8), keyReference);
        return Base64.getEncoder().encodeToString(encryptedText);
    }

    @Operation(summary = "Decrypts the Base64-Encoded String (which you copied) and returns the Plaintext")
    @PutMapping("/decrypt")
    public ResponseEntity<String> decrypt(@RequestParam String cipherTextBase64Encoded) {
        byte[] cipherText = Base64.getDecoder().decode(cipherTextBase64Encoded);
        byte[] decryptedText = keyReferenceCryptoService.decrypt(cipherText);
        return ResponseEntity.ok(new String(decryptedText, UTF_8));
    }
}
