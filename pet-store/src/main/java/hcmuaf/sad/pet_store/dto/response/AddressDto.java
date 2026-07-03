package hcmuaf.sad.pet_store.dto.response;

import lombok.Data;

@Data
public class AddressDto {
    private String addressId;
    private String recipientName;
    private String phone;
    private String placeId;
    private String fullAddress;
    private String addressDetail;
    private boolean isDefault;
}
