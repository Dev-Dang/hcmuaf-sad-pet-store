package hcmuaf.sad.pet_store.exception;

import lombok.Getter;

@Getter
public abstract class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    private Integer httpStatus;

    protected AppException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    protected AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.name(), cause);
        this.errorCode = errorCode;
    }

    public AppException withStatus(int status) {
        this.httpStatus = status;
        return this;
    }
}
