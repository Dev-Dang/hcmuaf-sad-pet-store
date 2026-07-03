package hcmuaf.sad.pet_store.model;

import java.math.BigDecimal;

public record OrderSummary(int orderCount, BigDecimal totalValue) {
}
