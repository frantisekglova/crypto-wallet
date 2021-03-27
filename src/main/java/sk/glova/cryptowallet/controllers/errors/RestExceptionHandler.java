package sk.glova.cryptowallet.controllers.errors;

import java.time.Instant;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sk.glova.cryptowallet.domain.response.ApiError;
import sk.glova.cryptowallet.exception.EntityNotFoundException;
import sk.glova.cryptowallet.exception.OperationNotAllowedException;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFoundEntity(HttpServletRequest request, EntityNotFoundException ex) {
        return new ResponseEntity<>(createApiError(ex).status(HttpStatus.NOT_FOUND.toString()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OperationNotAllowedException.class)
    public ResponseEntity<ApiError> handleUnprocessableEntity(HttpServletRequest request, OperationNotAllowedException ex) {
        return new ResponseEntity<>(createApiError(ex).status(HttpStatus.UNPROCESSABLE_ENTITY.toString()).build(), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private ApiError.ApiErrorBuilder createApiError(RuntimeException ex) {
        return ApiError.builder()
            .message(ex.getMessage())
            .exception(ex.getClass().getSimpleName())
            .timestamp(Instant.now());
    }

}
