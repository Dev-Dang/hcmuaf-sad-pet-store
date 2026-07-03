package hcmuaf.sad.pet_store.repository;

import hcmuaf.sad.pet_store.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUserCodeAndDeletedAtIsNull(String userCode);
    Optional<CartItem> findByUserCodeAndVariantIdAndDeletedAtIsNull(String userCode, Long variantId);
    Optional<CartItem> findByIdAndUserCodeAndDeletedAtIsNull(Long id, String userCode);
}
