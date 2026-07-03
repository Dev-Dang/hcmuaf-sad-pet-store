package hcmuaf.sad.pet_store.entity;

import hcmuaf.sad.pet_store.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Snapshot address at time of order
    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "shipping_fee", nullable = false)
    private BigDecimal shippingFee;

    @Column(name = "subtotal", nullable = false)
    private BigDecimal subtotal;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "order_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.NEW;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payment_method")
    private String paymentMethod = "COD";

    @Column(name = "note")
    private String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    public enum OrderStatus {
        NEW("Mới"),
        CONFIRMED("Đã xác nhận"),
        SHIPPING("Đang giao"),
        COMPLETED("Hoàn tất"),
        CANCELLED("Hủy"),
        PENDING_CANCEL("Chờ xác nhận hủy/hoàn tiền");

        private final String label;
        OrderStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum PaymentStatus {
        UNPAID("Chưa thanh toán"),
        PENDING("Đang xử lý"),
        PAID("Đã thanh toán"),
        FAILED("Thất bại"),
        TIMEOUT("Hết hạn"),
        REFUND_PENDING("Chờ hoàn tiền"),
        REFUNDED("Đã hoàn tiền");

        private final String label;
        PaymentStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }
}
