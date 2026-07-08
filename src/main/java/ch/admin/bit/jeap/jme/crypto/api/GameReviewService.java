package ch.admin.bit.jeap.jme.crypto.api;

import ch.admin.bit.jeap.jme.crypto.core.GameReview;
import ch.admin.bit.jeap.jme.crypto.core.GameReviewPublisher;
import ch.admin.bit.jeap.jme.crypto.core.GameReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameReviewService {

   private final GameReviewRepository gameReviewRepository;
   private final GameReviewPublisher gameReviewPublisher;

    public void createGameReview(String id, String author, String reviewText) {
        GameReview gameReview = GameReview.of(id, author, reviewText);
        this.gameReviewRepository.putGameReview(gameReview);
        this.gameReviewPublisher.publishGameReview(gameReview);
    }

    public GameReview getGameReview(String id) {
        return this.gameReviewRepository
                .getGameReview(id)
                .orElseThrow(() -> new IllegalArgumentException("GameReview with id: <" + id + "> not found!"));
    }
}
