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
    @Query(
            value = """
                    SELECT o.*
                    FROM orders o
                    WHERE o.user_id IN (SELECT id FROM users WHERE user_code = :userCode)
                      AND o.deleted_at IS NULL
                    ORDER BY o.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM orders o
                    WHERE o.user_id IN (SELECT id FROM users WHERE user_code = :userCode)
                      AND o.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Page<Order> findByUserCodeAndDeletedAtIsNullOrderByCreatedAtDesc(@Param("userCode") String userCode, Pageable pageable);

    // Customer: get order detail - must own it (BRULE-08)
    @Query(
            value = """
                    SELECT o.*
                    FROM orders o
                    WHERE o.id = :id
                      AND o.user_id IN (SELECT id FROM users WHERE user_code = :userCode)
                      AND o.deleted_at IS NULL
                    """,
            nativeQuery = true
    )
    Optional<Order> findByIdAndUserCodeAndDeletedAtIsNull(@Param("id") Long id, @Param("userCode") String userCode);

    // Admin: all orders
    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL ORDER BY o.createdAt DESC")
    Page<Order> findAllActiveOrders(Pageable pageable);
}
