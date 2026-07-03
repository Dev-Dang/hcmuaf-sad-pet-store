package hcmuaf.sad.pet_store.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends BusinessException {
    public InsufficientStockException(String variantName, int requestedQty, int availableQty) {
        super(ErrorCode.INSUFFICIENT_STOCK);
        withStatus(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}
