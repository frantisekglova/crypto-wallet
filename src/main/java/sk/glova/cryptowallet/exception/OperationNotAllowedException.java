package sk.glova.cryptowallet.exception;

/**
 * Custom Exception when request is violating the business logic
 */
public class OperationNotAllowedException extends RuntimeException {

    public OperationNotAllowedException(String message) {
        super(message);
    }

}
