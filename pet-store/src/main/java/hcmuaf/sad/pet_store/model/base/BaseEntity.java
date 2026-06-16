package hcmuaf.sad.pet_store.model.base;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public abstract class BaseEntity {
    protected Long id;
    protected LocalDateTime effectiveFrom;
    protected LocalDateTime effectiveTo;
    protected boolean isCurrent;
    protected boolean isDeleted;
    protected LocalDateTime createdAt;

    // Lombok sinh isCurrent() cho boolean — alias setter tường minh cho RowMapper
    public void setIsCurrent(boolean isCurrent) { this.isCurrent = isCurrent; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }

    public abstract void softDelete();
}
