package hcmuaf.sad.pet_store.exception.resource;

import hcmuaf.sad.pet_store.exception.base.BaseException;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String resource, Object id) {
        super(resource.toUpperCase() + "_NOT_FOUND", resource + " not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}
