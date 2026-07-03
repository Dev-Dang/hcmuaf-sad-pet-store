package hcmuaf.sad.pet_store.mapper;

import hcmuaf.sad.pet_store.dto.request.AddressRequest;
import hcmuaf.sad.pet_store.dto.response.AddressDto;
import hcmuaf.sad.pet_store.model.ShippingAddress;

import java.util.List;

public class AddressMapper {

    public static AddressDto toDto(ShippingAddress address) {
        AddressDto dto = new AddressDto();
        dto.setAddressId(address.getAddressId());
        dto.setRecipientName(address.getRecipientName());
        dto.setPhone(address.getPhone());
        dto.setPlaceId(address.getPlaceId());
        dto.setFullAddress(address.getFullAddress());
        dto.setAddressDetail(address.getAddressDetail());
        dto.setDefault(address.isDefault());
        return dto;
    }

    public static List<AddressDto> toDtoList(List<ShippingAddress> addresses) {
        return addresses.stream()
                .map(AddressMapper::toDto)
                .toList();
    }

    public static AddressRequest toRequest(ShippingAddress address) {
        AddressRequest request = new AddressRequest();
        request.setRecipientName(address.getRecipientName());
        request.setPhone(address.getPhone());
        request.setPlaceId(address.getPlaceId());
        request.setFullAddress(address.getFullAddress());
        request.setAddressDetail(address.getAddressDetail());
        return request;
    }
}
