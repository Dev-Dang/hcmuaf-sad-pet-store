package hcmuaf.sad.pet_store.mapper;

import hcmuaf.sad.pet_store.dto.response.ProductResponse;
import hcmuaf.sad.pet_store.dto.response.VariantResponse;
import hcmuaf.sad.pet_store.entity.Product;
import hcmuaf.sad.pet_store.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.name", target = "categoryName")
    ProductResponse toResponse(Product product);

    VariantResponse toVariantResponse(ProductVariant variant);
}
