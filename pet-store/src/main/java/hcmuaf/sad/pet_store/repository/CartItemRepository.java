package hcmuaf.sad.pet_store.repository;

import hcmuaf.sad.pet_store.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUserIdAndDeletedAtIsNull(Long userId);
    Optional<CartItem> findByUserIdAndVariantIdAndDeletedAtIsNull(Long userId, Long variantId);
    Optional<CartItem> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
