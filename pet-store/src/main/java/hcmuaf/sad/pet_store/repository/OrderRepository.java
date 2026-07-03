package hcmuaf.sad.pet_store.repository;

import hcmuaf.sad.pet_store.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Customer: orders for a specific user
    Page<Order> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Customer: get order detail - must own it (BRULE-08)
    Optional<Order> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    // Admin: all orders
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL ORDER BY o.createdAt DESC")
    Page<Order> findAllActiveOrders(Pageable pageable);
}
