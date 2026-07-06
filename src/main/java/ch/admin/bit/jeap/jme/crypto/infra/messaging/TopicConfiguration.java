package ch.admin.bit.jeap.jme.crypto.infra.messaging;


import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicCollection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;

@SuppressWarnings("java:S1118") // Suppress SonarLint warning about utility class with no public constructor
@Configuration
class TopicConfiguration {

    @Profile("cloud")
    @Configuration
    static class TopicConfigurationCloud {

        private final KafkaAdmin kafkaAdmin;
        private final String gameReviewCreatedTopicName;

        TopicConfigurationCloud(KafkaAdmin kafkaAdmin,
                                @Value("${jme.crypto.messaging.game-review-created-topic-name}") String gameReviewCreatedTopicName) {
            this.kafkaAdmin = kafkaAdmin;
            this.gameReviewCreatedTopicName = gameReviewCreatedTopicName;
        }

        @SneakyThrows
        @PostConstruct
        void checkIfTopicExist() {
            try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
                adminClient.describeTopics(TopicCollection.ofTopicNames(List.of(gameReviewCreatedTopicName)))
                        .topicNameValues()
                        .get(gameReviewCreatedTopicName)
                        .get();
            }
        }
    }

    @Profile("aws-vault")
    @Configuration
    static class TopicConfigurationLocalWithOnPremVault {

        @Value("${jme.crypto.messaging.game-review-created-topic-name}")
        private String gameReviewCreatedTopicName;

        @Bean
        NewTopic declarationCreatedTopic() {
            return new NewTopic(gameReviewCreatedTopicName, 1, (short) 1);
        }
    }

    @Profile("!cloud & !aws-vault")
    @Configuration
    static class TopicConfigurationLocal {

        @Value("${jme.crypto.messaging.game-review-created-topic-name}")
        private String gameReviewCreatedTopicName;

        @Bean
        NewTopic declarationCreatedTopic() {
            return new NewTopic(gameReviewCreatedTopicName, 1, (short) 1);
        }
    }
}
