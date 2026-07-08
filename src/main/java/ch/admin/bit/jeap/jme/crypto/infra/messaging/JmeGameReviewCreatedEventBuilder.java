package ch.admin.bit.jeap.jme.crypto.infra.messaging;

import ch.admin.bit.jeap.domainevent.avro.AvroDomainEventBuilder;
import ch.admin.bit.jeap.messaging.avro.AvroMessageBuilderException;
import ch.admin.bit.jme.crypto.JmeGameReviewAuthorReference;
import ch.admin.bit.jme.crypto.JmeGameReviewCreatedEvent;

import ch.admin.bit.jme.crypto.JmeGameReviewCreatedEventPayload;
import ch.admin.bit.jme.crypto.JmeGameReviewCreatedEventReferences;
import lombok.Getter;

@Getter
class JmeGameReviewCreatedEventBuilder extends AvroDomainEventBuilder<JmeGameReviewCreatedEventBuilder, JmeGameReviewCreatedEvent> {

    private final String systemName = "JME";
    private final String serviceName = "jme-crypto-service";
    private final String eventName = "JmeGameReviewCreatedEvent";

    private String reviewId;
    private String reviewAuthor;
    private String reviewText;

    private JmeGameReviewCreatedEventBuilder() {
        super(JmeGameReviewCreatedEvent::new);
    }

    static JmeGameReviewCreatedEventBuilder create() {
        return new JmeGameReviewCreatedEventBuilder();
    }

    JmeGameReviewCreatedEventBuilder reviewId(String reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    JmeGameReviewCreatedEventBuilder reviewAuthor(String reviewAuthor) {
        this.reviewAuthor = reviewAuthor;
        return this;
    }

    JmeGameReviewCreatedEventBuilder reviewText(String reviewText) {
        this.reviewText = reviewText;
        return this;
    }

    @Override
    protected JmeGameReviewCreatedEventBuilder self() {
        return this;
    }

    @Override
    public JmeGameReviewCreatedEvent build() {
        if (this.reviewId == null) {
            throw AvroMessageBuilderException.propertyNull("JmeGameReviewCreatedEventReferences.reviewId");
        }
        if (this.reviewAuthor == null) {
            throw AvroMessageBuilderException.propertyNull("JmeGameReviewCreatedEventReferences.reviewAuthor");
        }
        if (this.reviewText == null) {
            throw AvroMessageBuilderException.propertyNull("JmeGameReviewCreatedEventReferences.reviewText");
        }
        JmeGameReviewAuthorReference authorReference = JmeGameReviewAuthorReference.newBuilder().setReviewAuthor(reviewAuthor).build();
        JmeGameReviewCreatedEventReferences references = JmeGameReviewCreatedEventReferences.newBuilder().setAuthor(authorReference).build();
        JmeGameReviewCreatedEventPayload payload = JmeGameReviewCreatedEventPayload.newBuilder()
                .setReviewId(reviewId)
                .setReviewText(reviewText)
                .build();
        setReferences(references);
        setPayload(payload);
        return super.build();
    }

}
