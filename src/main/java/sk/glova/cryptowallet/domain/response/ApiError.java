package sk.glova.cryptowallet.domain.response;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {

    private String status;
    private String message;
    private String exception;
    private Instant timestamp;

}
