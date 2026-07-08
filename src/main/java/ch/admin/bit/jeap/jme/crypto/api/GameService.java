package ch.admin.bit.jeap.jme.crypto.api;

import ch.admin.bit.jeap.jme.crypto.core.Game;
import ch.admin.bit.jeap.jme.crypto.core.GameRepository;
import ch.admin.bit.jeap.jme.crypto.infra.api.rest.GameDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameService {

    private final GameRepository gameRepository;

    @Transactional
    public String createGame(String id, String name) {
        Game game = Game.of(id, name, name);
        gameRepository.save(game);
        log.info("Created a new Game <id: {}>", game.getId());
        return game.getId();
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public GameDto getGame(String id) {
        Game game =  gameRepository.findByIdOrFail(id);
        return GameDto.of(game.getId(), game.getName(), game.getEncryptedName());
    }
}
