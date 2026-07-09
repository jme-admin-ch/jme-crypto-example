package ch.admin.bit.jeap.jme.crypto.infra.messaging;

import ch.admin.bit.jeap.jme.crypto.core.GameReview;
import ch.admin.bit.jeap.jme.crypto.core.GameReviewPublisher;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContract;
import ch.admin.bit.jeap.messaging.avro.AvroMessage;
import ch.admin.bit.jeap.messaging.avro.AvroMessageKey;
import ch.admin.bit.jme.crypto.JmeGameReviewCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
@JeapMessageProducerContract(value = JmeGameReviewCreatedEvent.TypeRef.class,
        encryptionKeyId = "gameReviewMessaging",
        topic = "jme-crypto-game-review-created",
        appName = "jme-crypto-service")
class KafkaGameReviewPublisher implements GameReviewPublisher {

    private static final int SEND_TIMEOUT_SEC = 30;

    private final KafkaTemplate<AvroMessageKey, AvroMessage> kafkaTemplate;

    @Value("${jme.crypto.messaging.game-review-created-topic-name}")
    private String gameReviewCreatedTopicName;

    @Override
    public void publishGameReview(GameReview gameReview) {
        JmeGameReviewCreatedEvent event = JmeGameReviewCreatedEventBuilder.create()
                .reviewId(gameReview.getReviewId())
                .reviewAuthor(gameReview.getAuthor())
                .reviewText(gameReview.getPlaintext())
                .idempotenceId(gameReview.getReviewId())
                .build();
        publish(event);
        log.info("Published JmeGameReviewCreatedEvent for review with id '{}' and text '{}'.",
                event.getPayload().getReviewId(), event.getPayload().getReviewText());
    }

    private void publish(AvroMessage message) {
        try {
            kafkaTemplate.send(gameReviewCreatedTopicName, message)
                    .get(SEND_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Publishing interrupted", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Publishing timed out", e);
        } catch (Exception e) {
            throw new RuntimeException("Publishing failed for some reason", e);
        }
    }
}
