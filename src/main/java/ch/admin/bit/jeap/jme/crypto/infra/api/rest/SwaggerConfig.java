package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "jme-crypto-example",
                description = "Demo Rest-API for client-side encryption and decryption with the jeap-crypto-Library"
        ),
        externalDocs = @ExternalDocumentation(
                url = "https://confluence.bit.admin.ch/display/BLUE/jEAP+Crypto",
                description = "jEAP-Crypto in Confluence")
)
@Configuration
public class SwaggerConfig {

    @Bean
    GroupedOpenApi cryptoExampleRestApi() {
        return GroupedOpenApi.builder()
                .group("JME Crypto Example Rest API")
                .pathsToMatch("/api/**")
                .packagesToScan(this.getClass().getPackageName())
                .build();
    }
}
