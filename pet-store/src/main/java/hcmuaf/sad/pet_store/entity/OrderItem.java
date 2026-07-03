package hcmuaf.sad.pet_store.entity;

import hcmuaf.sad.pet_store.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Snapshot fields per BRULE-30
    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_name")
    private String variantName;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    // References (kept for data integrity but not relied on for display)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "variant_id")
    private Long variantId;
}
