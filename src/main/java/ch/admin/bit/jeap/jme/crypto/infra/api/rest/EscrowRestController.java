package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import ch.admin.bit.jeap.jme.crypto.api.EscrowDecryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

@Tag(name = "Escrow Key Test Resource",
        description = "Demonstrates escrow data key decryption (requires ciphertext to be encrypted with AWS KMS)")
@RestController
@RequestMapping("/api/escrow")
@Slf4j
@RequiredArgsConstructor
public class EscrowRestController {

    private final EscrowDecryptionService escrowDecryptionService;

    @Operation(
            summary = """
                    Decrypts the Base64-Encoded ciphertext container using the provided escrow private key,
                    and returns the plaintext. This function does not use the KMS, and relies only on the escrow key
                    for decryption.

                    Note: This is for demonstration purposes only. A production-ready decryption function would not export
                    the key from Vault, but use Vault's transit secrets engine to decrypt data keys using the escrow key instead.""",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = """
                            PKCS#1 encoded private key or JSON key export from vault
                            When using PKCS#1: This key MUST start with the line -----BEGIN RSA PRIVATE KEY-----.
                            You may also directly paste the JSON export from Vault here."""),
            parameters = @Parameter(
                    name = "dataContainerBase64",
                    required = true,
                    description = """
                            Base64-encoded jEAP Crypt Data Container (generated using AWS KMS), which includes a
                            data key encrypted with the escrow key"""))
    @PutMapping("/decrypt")
    public String decrypt(@RequestParam("dataContainerBase64") String dataContainerBase64,
                          @RequestBody String privateKeyString) {
        byte[] dataContainerBytes = Base64.getDecoder().decode(dataContainerBase64);
        RSAPrivateKey rsaPrivateKey = KeyParameterParser.parsePrivateKeyParameter(privateKeyString);
        byte[] decryptedText = escrowDecryptionService.decrypt(dataContainerBytes, rsaPrivateKey);
        return new String(decryptedText, UTF_8);
    }
}
