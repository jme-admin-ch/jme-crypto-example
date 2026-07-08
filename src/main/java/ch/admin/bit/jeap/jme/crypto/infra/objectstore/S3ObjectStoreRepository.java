package ch.admin.bit.jeap.jme.crypto.infra.objectstore;

import ch.admin.bit.jeap.crypto.api.CryptoService;
import ch.admin.bit.jeap.crypto.s3.JeapCryptoS3Template;
import ch.admin.bit.jeap.crypto.s3.JeapDecryptedS3Object;
import ch.admin.bit.jeap.jme.crypto.core.GameReview;
import ch.admin.bit.jeap.jme.crypto.core.GameReviewRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.urlconnection.ProxyConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Repository
@RequiredArgsConstructor
public class S3ObjectStoreRepository implements GameReviewRepository {

    private static final String METADATA_KEY_AUTHOR = "author";
    private S3Client s3Client;

    private final S3ObjectStorageConnectionProperties connectionProperties;

    /**
     * The jEAPCryptoS3Template: Simplifies the encryption/decryption for S3. Must be instantiated after
     * the S3Client is initialized.
     */
    private JeapCryptoS3Template jeapCryptoS3Template;

    /**
     * Attention! Here is the Magic:
     * <p>
     * You choose a Name for a CryptoService in application.yaml under jeap.crypto.vault.keys.
     * In this example it's:
     * - gameReviewObjectStore
     * - gameDatabase
     * <p>
     * The jeap-crypto-vault-starter creates in this case 2 Beans with the Postfix 'CryptoService':
     * - gameReviewObjectStoreCryptoService
     * - gameDatabaseCryptoService
     * <p>
     * Inject this bean with @Qualifier (or by Name) and you can encrypt/decrypt in the client.
     */
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Qualifier("gameReviewObjectStoreCryptoService")
    @Autowired
    private CryptoService cryptoService;

    /**
     * Initializing the S3-Client with connection properties
     * Creates a JeapCryptoS3Template
     */
    @PostConstruct
    private void initS3ClientConnection() {
        log.info("Initializing s3Client with connection properties {}.", connectionProperties.toString());

        try {
            AwsCredentialsProvider credentialsProvider = createCredentialsProvider();

            ClientOverrideConfiguration.Builder overrideConfig = ClientOverrideConfiguration.builder();
            overrideConfig.advancedOptions(Map.of(SdkAdvancedClientOption.SIGNER, AwsS3V4Signer.create()));

            s3Client = S3Client.builder()
                    .endpointOverride(retrieveEndpointURI(connectionProperties.getAccessUrl()))
                    .region(connectionProperties.region())
                    .forcePathStyle(true)
                    .credentialsProvider(credentialsProvider)
                    .httpClient(UrlConnectionHttpClient.builder()
                            .proxyConfiguration(ProxyConfiguration.builder()
                                    .useSystemPropertyValues(false)
                                    .useEnvironmentVariablesValues(false)
                                    .build())
                            .build())
                    .overrideConfiguration(overrideConfig.build())
                    .build();

            if (connectionProperties.isCheckConnectionOnStartupRequired()) {
                log.info("Checking connection ... ");
                doesBucketExist("bucket-to-use-for-connection-check");
                log.info("... connection check successful.");
            }

            // Creates the JeapCryptoS3Template here
            jeapCryptoS3Template = new JeapCryptoS3Template(s3Client, cryptoService);

            log.info("The initialization of s3Client was successful!");

        } catch (SdkClientException e) {
            throw S3ObjectStorageException.connectingFailed(e, connectionProperties);
        }
    }

    private AwsCredentialsProvider createCredentialsProvider() {
        if (connectionProperties.getAccessKey() == null) {
            return DefaultCredentialsProvider.create();
        }
        return StaticCredentialsProvider
                .create(AwsBasicCredentials.create(connectionProperties.getAccessKey(), connectionProperties.getSecretKey()));
    }

    private URI retrieveEndpointURI(String accessUrl) {
        if (accessUrl == null) {
            return null;
        }
        if (accessUrl.startsWith("http://") || accessUrl.startsWith("https://")) {
            return URI.create(accessUrl);
        }
        return URI.create("https://" + accessUrl);
    }

    /**
     * Store the GameReview encrypted on S3. Encryption happens in jEAPCryptoTemplate.
     * Stores the author as Metadata in teh S3-Object.
     * If the bucket not exist it will be created.
     *
     * @param gameReview - A silly use case to store something encrypted on S3
     */
    @Override
    public void putGameReview(GameReview gameReview) {

        String bucketName = connectionProperties.getBucketName();
        if (!this.doesBucketExist(bucketName)) {
            this.createBucket(bucketName);
        }

        Map<String, String> objectMetaData = Map.of(METADATA_KEY_AUTHOR, gameReview.getAuthor());

        jeapCryptoS3Template.putObject(bucketName,
                gameReview.getReviewId(),
                gameReview.getPlaintext().getBytes(UTF_8),
                objectMetaData);
    }

    /**
     * Retrieve the GameReview (or not) from S3. Decryption happens in jeapCryptoS3Template.
     * Get the author from the Object-Metadata (key=author) and add it the GameReview
     *
     * @param objectKey - ObjectId (String) on S3
     * @return a GameReview-Object (Optional)
     */
    @Override
    public Optional<GameReview> getGameReview(String objectKey) {

        String bucketName = connectionProperties.getBucketName();
        if (!doesObjectExistByMetaData(bucketName, objectKey)) {
            return Optional.empty();
        }

        JeapDecryptedS3Object jeapDecryptedS3Object = jeapCryptoS3Template.getObject(bucketName, objectKey);

        // The decryptedText is a byte-Array. Convert a Byte-Array with new String...not with .toString() !
        String decryptedTextAsString = new String(jeapDecryptedS3Object.getDecryptedObjectContent(), UTF_8);
        Map<String, String> objectMetadata = jeapDecryptedS3Object.getMetadata();
        String author = objectMetadata.get(METADATA_KEY_AUTHOR);

        return Optional.of(GameReview.of(jeapDecryptedS3Object.getObjectKey(), author, decryptedTextAsString));

    }

    private void createBucket(String bucketName) {
        if (doesBucketExist(bucketName)) {
            log.info("Bucket name already in use. Try another name.");
            return;
        }
        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(bucketName).build();
        s3Client.createBucket(createBucketRequest);
    }

    private boolean doesBucketExist(String bucketName) {
        log.debug("Checking if bucket '{}' exists.", bucketName);
        try {
            return doesBucketExistsByMetaData(bucketName);
        } catch (SdkClientException e) {
            throw S3ObjectStorageException.checkingIfBucketExistsFailed(e, bucketName);
        }
    }

    boolean doesObjectExistByMetaData(String bucket, String keyName) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(keyName).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    boolean doesBucketExistsByMetaData(String bucket) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        }
    }
}
