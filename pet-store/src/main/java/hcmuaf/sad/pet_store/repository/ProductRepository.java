package hcmuaf.sad.pet_store.repository;

import hcmuaf.sad.pet_store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByStatusAndDeletedAtIsNull(Product.Status status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.deletedAt IS NULL AND p.category.id = :categoryId")
    Page<Product> findAllActiveByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.deletedAt IS NULL AND LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchActiveByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.status = 'ACTIVE' AND p.deletedAt IS NULL")
    Optional<Product> findActiveById(@Param("id") Long id);
}
