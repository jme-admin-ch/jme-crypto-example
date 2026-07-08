package ch.admin.bit.jeap.jme.crypto.core;

import java.util.List;
import java.util.Optional;

public interface GameRepository {

    Game save(Game game);
    Optional<Game> findById(String id);

    List<Game> findAll();

    default Game findByIdOrFail(String id) {
        return findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Game with id: <" + id + "> not found!"));
    }

}
