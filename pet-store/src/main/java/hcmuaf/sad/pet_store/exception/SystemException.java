package hcmuaf.sad.pet_store.exception;

public final class SystemException extends AppException {
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
