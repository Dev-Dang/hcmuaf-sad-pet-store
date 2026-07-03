package hcmuaf.sad.pet_store.mapper;

import hcmuaf.sad.pet_store.dto.response.CartItemResponse;
import hcmuaf.sad.pet_store.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "variant.id", target = "variantId")
    @Mapping(source = "variant.product.name", target = "productName")
    @Mapping(source = "variant.name", target = "variantName")
    @Mapping(source = "variant.price", target = "unitPrice")
    CartItemResponse toResponse(CartItem cartItem);
}
