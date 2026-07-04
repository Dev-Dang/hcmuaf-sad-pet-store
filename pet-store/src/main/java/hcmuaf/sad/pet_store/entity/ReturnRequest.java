package hcmuaf.sad.pet_store.entity;

import hcmuaf.sad.pet_store.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "return_requests")
@EqualsAndHashCode(callSuper = true)
public class ReturnRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false,length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(length = 500)
    private String rejectReason;

    private LocalDateTime processedAt;

    private Long processedBy;

    public enum Status{
        PENDING,
        APPROVED,
        REJECTED
    }

}