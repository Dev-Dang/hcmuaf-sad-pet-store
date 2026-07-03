package hcmuaf.sad.pet_store.repository;

import hcmuaf.sad.pet_store.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findAllByProductIdAndStatusAndDeletedAtIsNull(Long productId, ProductVariant.Status status);
    
    Optional<ProductVariant> findByIdAndStatusAndDeletedAtIsNull(Long id, ProductVariant.Status status);
}
