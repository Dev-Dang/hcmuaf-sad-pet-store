package hcmuaf.sad.pet_store.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends SystemException {
    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND);
        withStatus(HttpStatus.NOT_FOUND.value());
    }
}
