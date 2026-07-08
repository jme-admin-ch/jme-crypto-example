package ch.admin.bit.jeap.jme.crypto.infra.api.rest;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "of")
public class GameDto {
    String id;
    String name;
    String encryptedName;
}
