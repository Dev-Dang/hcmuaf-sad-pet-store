package hcmuaf.sad.pet_store.service;

import hcmuaf.sad.pet_store.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductResponse> getAllActiveProducts(Long categoryId, Pageable pageable);
    Page<ProductResponse> searchActiveProducts(String keyword, Pageable pageable);
    ProductResponse getActiveProductDetails(Long id);
}
