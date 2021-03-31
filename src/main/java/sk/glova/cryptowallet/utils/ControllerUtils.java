package sk.glova.cryptowallet.utils;

import java.net.URI;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class ControllerUtils {

    public static URI getCurrentLocationWithId(Object id) {
        return ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
    }

}
