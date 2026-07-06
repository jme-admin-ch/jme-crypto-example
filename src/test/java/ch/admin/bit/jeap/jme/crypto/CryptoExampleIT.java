package ch.admin.bit.jeap.jme.crypto;

import ch.admin.bit.jeap.jme.test.BootServiceSpringIntegrationTestBase;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@TestPropertySource(properties = {
        "jeap.vault.url=http://vault:8200"
})
class CryptoExampleIT extends BootServiceSpringIntegrationTestBase {

    private static final String SERVICE_BASE_URL = "http://localhost:8080/jme-crypto-service";

    @BeforeAll
    static void startServices() throws Exception {
        startService(SERVICE_BASE_URL);
    }

    @Test
    void encryptAndDecryptString() {
        final String valueToEncrypt = "my value to encrypt";

        //encrypt
        Response encryptedValueResponse = given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().put("/api/crypto/encrypt?plaintext=" + valueToEncrypt);

        //decrypt
        Response decryptedValueResponse = given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().put("/api/crypto/decrypt?cipherTextBase64Encoded=" + encryptedValueResponse.getBody().asString());

        assertThat(decryptedValueResponse.getBody().asString()).isEqualTo(valueToEncrypt);
    }

    @Test
    void encryptAndDecryptDb() {
        final String id = "9999";
        final String nameToEncrypt = "my name to encrypt";

        //encrypt
        given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().put("/api/games/" + id + "?name=" + nameToEncrypt)
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().get("/api/games/" + id);

        JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();

        assertThat((String) jsonPathEvaluator.get("id")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("name")).isEqualTo(nameToEncrypt);
        assertThat((String) jsonPathEvaluator.get("encryptedName")).isEqualTo(nameToEncrypt);
    }

    @Test
    void encryptAndDecryptDb_nullValue() {
        final String id = "9999";

        //encrypt
        given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().put("/api/games/" + id)
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().get("/api/games/" + id);

        JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();

        assertThat((String) jsonPathEvaluator.get("id")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("name")).isNull();
        assertThat((String) jsonPathEvaluator.get("encryptedName")).isNull();
    }

    @Test
    void encryptAndDecryptDb_emptyString() {
        final String id = "9999";

        //encrypt
        given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().put("/api/games/" + id + "?emptyName=true")
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().get("/api/games/" + id);

        JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();

        assertThat((String) jsonPathEvaluator.get("id")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("name")).isEmpty();
        assertThat((String) jsonPathEvaluator.get("encryptedName")).isEmpty();
    }

    @Test
    void encryptAndDecryptS3() {
        final String id = "9999";
        final String authorToEncrypt = "my author to encrypt";
        final String reviewTextToEncrypt = "my reviewText to encrypt";

        //encrypt
        given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().put("/api/gamereviews/" + id + "?author=" + authorToEncrypt + "&reviewText=" + reviewTextToEncrypt)
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().baseUri(SERVICE_BASE_URL).contentType(ContentType.JSON)
                .when().get("/api/gamereviews/" + id);

        final JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();
        assertThat((String) jsonPathEvaluator.get("reviewId")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("author")).isEqualTo(authorToEncrypt);
        assertThat((String) jsonPathEvaluator.get("plaintext")).isEqualTo(reviewTextToEncrypt);
    }

}
