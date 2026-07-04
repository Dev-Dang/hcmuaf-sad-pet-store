package hcmuaf.sad.pet_store.entity;

import hcmuaf.sad.pet_store.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "order_status_history")
@EqualsAndHashCode(callSuper = true)
public class OrderStatusHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id",nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus newStatus;

    private LocalDateTime changedAt;

    private Long changedBy;

}
