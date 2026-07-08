package ch.admin.bit.jeap.jme.crypto.infra.objectstore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

@Getter
@Setter
@ToString
@ConfigurationProperties("jme.crypto.objectstorage.connection")
public class S3ObjectStorageConnectionProperties {

    private String accessUrl;

    // excluded from toString for security reasons
    @ToString.Exclude
    private String accessKey;

    // excluded from toString for security reasons
    @ToString.Exclude
    private String secretKey;

    private String bucketName = "bit-jme-crypto-game-reviews";

    private boolean checkConnectionOnStartupRequired = true;

    private String region = Region.AWS_GLOBAL.id();

    Region region() {
        return Region.of(region);
    }
}
