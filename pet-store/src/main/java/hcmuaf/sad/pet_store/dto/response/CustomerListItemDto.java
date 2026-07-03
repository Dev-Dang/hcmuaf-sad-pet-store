package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerListItemDto {
    private String userCode;
    private String displayName;
    private String email;
    private LocalDateTime createdAt;
}
