package ch.admin.bit.jeap.jme.crypto.core;

import ch.admin.bit.jeap.crypto.db.JeapCryptoStringConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class Game {

    @Id
    private String id;

    private String name;

    @Convert(converter = JeapCryptoStringConverter.class)
    private String encryptedName;

}
