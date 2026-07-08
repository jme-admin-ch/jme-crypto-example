package ch.admin.bit.jeap.jme.crypto.infra.jpa;

import ch.admin.bit.jeap.jme.crypto.core.Game;
import ch.admin.bit.jeap.jme.crypto.core.GameRepository;
import org.springframework.data.repository.CrudRepository;

public interface JpaGameRepository extends CrudRepository<Game, String>, GameRepository {
}
