package ch.admin.bit.jeap.jme.crypto.infra.messaging;

import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jme.crypto.JmeGameReviewCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
// Depending on the active Maven profile, the values for 'topic' and 'appName' in the annotations are adjusted.
// This is necessary because only one message contract per event can exist for each encryption key.
// The maven-antrun-plugin is used to modify the source code files before compilation.
@JeapMessageConsumerContract(value = JmeGameReviewCreatedEvent.TypeRef.class,
        topic = "jme-crypto-game-review-created",
        appName = "jme-crypto-service")
class KafkaGameReviewConsumer {

    @KafkaListener(topics = {"${jme.crypto.messaging.game-review-created-topic-name}"})
    void consume(JmeGameReviewCreatedEvent event, Acknowledgment ack) {
        log.info("Received JmeGameReviewCreatedEvent with review id '{}' and text '{}'.",
                event.getPayload().getReviewId(), event.getPayload().getReviewText());
        ack.acknowledge();
    }
}
