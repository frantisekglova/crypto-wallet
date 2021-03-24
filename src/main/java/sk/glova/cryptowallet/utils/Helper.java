package sk.glova.cryptowallet.utils;

import java.net.URI;
import java.util.EnumSet;
import java.util.stream.Collectors;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class Helper {

    public static <E extends Enum<E>> String getStringFromEnums(Class<E> clazz, String delimeter) {
        return EnumSet.allOf(clazz).stream()
            .map(Enum::toString)
            .collect(Collectors.joining(","));
    }

    public static URI getCurrentLocationWithId(Object id) {
        return ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(id)
            .toUri();
    }

}
