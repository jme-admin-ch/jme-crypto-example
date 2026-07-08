package ch.admin.bit.jeap.jme.crypto.core;

import java.util.Optional;

public interface GameReviewRepository {

    void putGameReview(GameReview gameReview);

    Optional<GameReview> getGameReview(String objectName);


}


