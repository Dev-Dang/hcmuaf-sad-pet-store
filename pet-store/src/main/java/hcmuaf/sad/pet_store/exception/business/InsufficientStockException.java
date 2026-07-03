package hcmuaf.sad.pet_store.exception.business;

import hcmuaf.sad.pet_store.exception.base.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BaseException {
    public InsufficientStockException(String variantName, int requestedQty, int availableQty) {
        super("INSUFFICIENT_STOCK", "Not enough stock for " + variantName + ". Requested: " + requestedQty + ", Available: " + availableQty, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
