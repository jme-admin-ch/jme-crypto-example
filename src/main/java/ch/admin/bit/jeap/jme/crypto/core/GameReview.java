package ch.admin.bit.jeap.jme.crypto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class GameReview {
    private String reviewId;
    private String author;
    private String plaintext;
}
