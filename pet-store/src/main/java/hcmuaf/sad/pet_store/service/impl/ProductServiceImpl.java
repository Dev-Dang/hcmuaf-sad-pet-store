package hcmuaf.sad.pet_store.service.impl;

import hcmuaf.sad.pet_store.dto.response.ProductResponse;
import hcmuaf.sad.pet_store.dto.response.VariantResponse;
import hcmuaf.sad.pet_store.entity.Product;
import hcmuaf.sad.pet_store.entity.ProductVariant;
import hcmuaf.sad.pet_store.exception.ResourceNotFoundException;
import hcmuaf.sad.pet_store.mapper.ProductMapper;
import hcmuaf.sad.pet_store.repository.ProductRepository;
import hcmuaf.sad.pet_store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public Page<ProductResponse> getAllActiveProducts(Long categoryId, Pageable pageable) {
        Page<Product> products;
        if (categoryId != null) {
            products = productRepository.findAllActiveByCategoryId(categoryId, pageable);
        } else {
            products = productRepository.findAllByStatusAndDeletedAtIsNull(Product.Status.ACTIVE, pageable);
        }
        return products.map(this::mapToResponseWithActiveVariants);
    }

    @Override
    public Page<ProductResponse> searchActiveProducts(String keyword, Pageable pageable) {
        return productRepository.searchActiveByName(keyword, pageable)
                .map(this::mapToResponseWithActiveVariants);
    }

    @Override
    public ProductResponse getActiveProductDetails(Long id) {
        Product product = productRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return mapToResponseWithActiveVariants(product);
    }

    private ProductResponse mapToResponseWithActiveVariants(Product product) {
        ProductResponse response = productMapper.toResponse(product);
        if (product.getVariants() != null) {
            List<VariantResponse> activeVariants = product.getVariants().stream()
                    .filter(v -> v.getStatus() == ProductVariant.Status.ACTIVE 
                              && v.getDeletedAt() == null
                              && v.getPrice().compareTo(BigDecimal.ZERO) > 0)
                    .map(productMapper::toVariantResponse)
                    .collect(Collectors.toList());
            response.setVariants(activeVariants);
        }
        return response;
    }
}
