package ch.admin.bit.jeap.jme.crypto.infra.messaging;

import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jme.crypto.JmeGameReviewCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
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
