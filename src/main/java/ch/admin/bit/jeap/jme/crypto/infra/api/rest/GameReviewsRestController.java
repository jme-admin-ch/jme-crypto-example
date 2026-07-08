package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import ch.admin.bit.jeap.jme.crypto.api.GameReviewService;
import ch.admin.bit.jeap.jme.crypto.core.GameReview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "GameReview Resource", description = "Encrypts the 'reviewText' and stores it in S3-ObjectStore and vice versa.")
@RestController
@RequestMapping("/api/gamereviews")
@RequiredArgsConstructor
@Slf4j
public class GameReviewsRestController {

    private final GameReviewService gameReviewService;

    @Operation(summary = "Create a GameReview Object on S3 with encrypted ReviewText")
    @PutMapping(value = "/{id}")
    public void createGameReview(@PathVariable String id, @RequestParam String author, @RequestParam String reviewText) {
        gameReviewService.createGameReview(id, author, reviewText);
    }

    @Operation(summary = "Loads the GameReview Object from S3 and decrypt the ReviewText.")
    @GetMapping(value = "/{id}")
    public ResponseEntity<GameReview> getGameReview(@PathVariable String id) {
        return ResponseEntity.ok(gameReviewService.getGameReview(id));
    }
}
