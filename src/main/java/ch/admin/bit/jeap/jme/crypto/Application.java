package ch.admin.bit.jeap.jme.crypto;

import ch.admin.bit.jeap.jme.crypto.infra.objectstore.S3ObjectStorageConnectionProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableConfigurationProperties(S3ObjectStorageConnectionProperties.class)
@Slf4j
public class Application {

    public static void main(String[] args) {
        Environment env = SpringApplication.run(Application.class, args).getEnvironment();

        log.info("""

                        ----------------------------------------------------------
                        \t{} is running!\s
                        \t
                        \tSwaggerUI: \thttp://localhost:{}{}/swagger-ui.html
                        \tProfile(s): \t{}
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"),
                env.getActiveProfiles());
    }
}
