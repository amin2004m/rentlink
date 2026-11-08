package rentlink.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public static BusinessException notFound(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.NOT_FOUND);
    }

    public static BusinessException conflict(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.CONFLICT);
    }

    public static BusinessException badRequest(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    public static BusinessException forbidden(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.FORBIDDEN);
    }

    public static BusinessException internal(String errorCode, String message) {
        return new BusinessException(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
