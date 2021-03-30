package sk.glova.cryptowallet.exception;

/**
 * Custom Exception when entity do not exist
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

}
