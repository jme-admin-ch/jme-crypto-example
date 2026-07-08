package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import ch.admin.bit.jeap.jme.crypto.api.GameService;
import ch.admin.bit.jeap.jme.crypto.core.Game;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Games Resource", description = "Encrypts the 'textToEncrypt' and stores it in the Database and vice versa.")
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Slf4j
public class GamesRestController {

    private final GameService gameService;

    @Operation(summary = "Creates a Game, encrypts the name and stores it on the database")
    @PutMapping("/{id}")
    public ResponseEntity<String> createGame(@PathVariable String id, @RequestParam(required = false) String name, @RequestParam(defaultValue = "false") boolean emptyName) {
        return ResponseEntity.ok(gameService.createGame(id, emptyName ? "" : name));
    }

    @Operation(summary = "Get a Game by Id and decrypt the cipherText")
    @GetMapping("/{id}")
    public ResponseEntity<GameDto> getGameById(@PathVariable String id) {
        return ResponseEntity.ok(gameService.getGame(id));
    }

    @Operation(summary = "Get all Games with decrypted cipherText")
    @GetMapping("/")
    public ResponseEntity<List<Game>> all() {
        return ResponseEntity.ok(gameService.getAllGames());
    }
}
