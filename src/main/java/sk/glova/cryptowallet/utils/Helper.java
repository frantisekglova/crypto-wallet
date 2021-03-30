package sk.glova.cryptowallet.utils;

import java.net.URI;
import java.util.EnumSet;
import java.util.stream.Collectors;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

// TODO this class is too general, methods do very different things, should be split
public class Helper {

    public static <E extends Enum<E>> String getStringFromEnums(Class<E> clazz, String delimeter) {
        return EnumSet.allOf(clazz).stream()
            .map(Enum::toString)
            .collect(Collectors.joining(delimeter));
    }

    public static URI getCurrentLocationWithId(Object id) {
        return ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
    }

}
