package ch.admin.bit.jeap.jme.crypto;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIfSystemProperty(named = AfterDeploymentSmokeTestIT.DEPLOY_STAGE_PROPERTY_NAME, matches = "dev")
class AfterDeploymentSmokeTestIT {
    static final String DEPLOY_STAGE_PROPERTY_NAME = "deployStage";
    private static final String TARGET_SERVICE = "targetService";
    static final String AWS_BASE_URI = "https://jme-%s.ingress.nivel.bazg.admin.ch";
    static final int TEN_SECONDS_IN_MS = 10000;

    private RequestSpecification request;

    @Test
    void encryptAndDecryptString() {

        final String valueToEncrypt = "my value to encrypt";

        //encrypt
        Response encryptedValueResponse = given().spec(request).contentType(ContentType.JSON)
                .when().put("/crypto/encrypt?plaintext=" + valueToEncrypt);

        //decrypt
        Response decryptedValueResponse = given().spec(request).contentType(ContentType.JSON)
                .when().put("/crypto/decrypt?cipherTextBase64Encoded=" + encryptedValueResponse.getBody().asString());

        assertThat(decryptedValueResponse.getBody().asString()).isEqualTo(valueToEncrypt);
    }

    @Test
    void encryptAndDecryptDb() {

        final String id = "9999";
        final String nameToEncrypt = "my name to encrypt";

        //encrypt
        given().spec(request).contentType(ContentType.JSON)
                .when().put("/games/" + id + "?name=" + nameToEncrypt)
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().spec(request).contentType(ContentType.JSON)
                .when().get("/games/" + id);

        JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();

        assertThat((String) jsonPathEvaluator.get("id")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("name")).isEqualTo(nameToEncrypt);
        assertThat((String) jsonPathEvaluator.get("encryptedName")).isEqualTo(nameToEncrypt);
    }

    @Test
    void encryptAndDecryptDb_nullValue() {

        final String id = "9999";

        //encrypt
        given().spec(request).contentType(ContentType.JSON)
                .when().put("/games/" + id)
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().spec(request).contentType(ContentType.JSON)
                .when().get("/games/" + id);

        JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();

        assertThat((String) jsonPathEvaluator.get("id")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("name")).isNull();
        assertThat((String) jsonPathEvaluator.get("encryptedName")).isNull();
    }

    @Test
    void encryptAndDecryptDb_emptyString() {

        final String id = "9999";

        //encrypt
        given().spec(request).contentType(ContentType.JSON)
                .when().put("/games/" + id + "?emptyName=true")
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().spec(request).contentType(ContentType.JSON)
                .when().get("/games/" + id);

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
        given().spec(request).contentType(ContentType.JSON)
                .when().put("/gamereviews/" + id + "?author=" + authorToEncrypt + "&reviewText=" + reviewTextToEncrypt)
                .then()
                .statusCode(HttpStatus.OK.value());

        //decrypt
        Response decryptedValueResponse = given().spec(request).contentType(ContentType.JSON)
                .when().get("/gamereviews/" + id);

        final JsonPath jsonPathEvaluator = decryptedValueResponse.jsonPath();
        assertThat((String) jsonPathEvaluator.get("reviewId")).isEqualTo(id);
        assertThat((String) jsonPathEvaluator.get("author")).isEqualTo(authorToEncrypt);
        assertThat((String) jsonPathEvaluator.get("plaintext")).isEqualTo(reviewTextToEncrypt);
    }

    @BeforeEach
    void setUp() {
        String deployStage = System.getProperty(DEPLOY_STAGE_PROPERTY_NAME, "dev");
        String baseUri = AWS_BASE_URI.formatted(deployStage);
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config().httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.socket.timeout", TEN_SECONDS_IN_MS)
                .setParam("http.connection.timeout", TEN_SECONDS_IN_MS));
        RestAssured.config.getLogConfig().blacklistHeader(HttpHeaders.AUTHORIZATION, HttpHeaders.SET_COOKIE);
        RestAssured.filters(new ResponseLoggingFilter());

        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri(baseUri);

        String targetService = System.getProperty(TARGET_SERVICE, "jme-crypto-service");
        log.info("Target service: {}", targetService);
        builder.setBasePath("/" + targetService + "/api");
        request = builder.build();
    }

}
