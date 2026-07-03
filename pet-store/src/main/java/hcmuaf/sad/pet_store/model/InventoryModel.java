package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.dto.response.ProductRow;
import hcmuaf.sad.pet_store.dto.response.StockAlert;
import hcmuaf.sad.pet_store.util.DBUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

public class InventoryModel {

    public void importProduct(String name, String description, BigDecimal price, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng nhập phải lớn hơn 0");
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá tiền không được âm");
        }

        DBUtils.tx().execute(status -> {
            Long productId = findProductIdByName(name);

            if (productId != null) {
                DBUtils.jdbc().update("""
                    UPDATE products
                    SET quantity = quantity + ?,
                        price = ?,
                        updated_at = NOW()
                    WHERE id = ?
                """, quantity, price, productId);
            } else {
                productId = insertProduct(name, description, price, quantity);
            }

            DBUtils.jdbc().update("""
                INSERT INTO inventories(product_id, import_date, quantity, note)
                VALUES (?, NOW(), ?, ?)
            """, productId, quantity, "Nhập kho");

            return null;
        });
    }

    private Long findProductIdByName(String name) {
        return DBUtils.jdbc().query("""
            SELECT id
            FROM products
            WHERE name = ?
            LIMIT 1
        """, rs -> rs.next() ? rs.getLong("id") : null, name);
    }

    private Long insertProduct(String name, String description, BigDecimal price, int quantity) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        DBUtils.jdbc().update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO products(
                    name,
                    description,
                    price,
                    quantity,
                    stock_threshold,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, 5, 'ACTIVE', NOW(), NOW())
            """, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBigDecimal(3, price);
            ps.setInt(4, quantity);

            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public ProductRow findProduct(Long id) {
        return DBUtils.jdbc().queryForObject("""
            SELECT id, name, price, quantity
            FROM products
            WHERE id = ?
        """, (rs, row) -> {
            ProductRow p = new ProductRow();
            p.setId(rs.getLong("id"));
            p.setName(rs.getString("name"));
            p.setPrice(rs.getBigDecimal("price"));
            p.setQuantity(rs.getInt("quantity"));
            return p;
        }, id);
    }

    public List<ProductRow> findAllProducts() {
        return DBUtils.jdbc().query("""
            SELECT id, name, price, quantity
            FROM products
            ORDER BY name
        """, (rs, row) -> {
            ProductRow p = new ProductRow();
            p.setId(rs.getLong("id"));
            p.setName(rs.getString("name"));
            p.setPrice(rs.getBigDecimal("price"));
            p.setQuantity(rs.getInt("quantity"));
            return p;
        });
    }

    public void updateQuantity(Long id, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm");
        }

        DBUtils.jdbc().update("""
            UPDATE products
            SET quantity = ?,
                updated_at = NOW()
            WHERE id = ?
        """, quantity, id);
    }

    public List<StockAlert> findLowStockProducts() {
        return DBUtils.jdbc().query("""
            SELECT
                id,
                name,
                price,
                quantity,
                stock_threshold
            FROM products
            WHERE quantity <= stock_threshold
            ORDER BY quantity ASC
        """, (rs, row) -> {
            StockAlert item = new StockAlert();
            item.setId(rs.getLong("id"));
            item.setName(rs.getString("name"));
            item.setPrice(rs.getBigDecimal("price"));
            item.setQuantity(rs.getInt("quantity"));
            item.setStockThreshold(rs.getInt("stock_threshold"));
            return item;
        });
    }

    public List<ProductThresholdRow> findAllThresholdProducts() {
        return DBUtils.jdbc().query("""
            SELECT id, name, price, quantity, stock_threshold
            FROM products
            ORDER BY name
        """, (rs, rowNum) -> {
            ProductThresholdRow p = new ProductThresholdRow();
            p.setId(rs.getLong("id"));
            p.setName(rs.getString("name"));
            p.setPrice(rs.getBigDecimal("price"));
            p.setQuantity(rs.getInt("quantity"));
            p.setStockThreshold(rs.getInt("stock_threshold"));
            return p;
        });
    }

    public void updateStockThreshold(Long id, int stockThreshold) {
        if (stockThreshold < 0) {
            throw new IllegalArgumentException("Ngưỡng tồn kho không được âm");
        }

        DBUtils.jdbc().update("""
            UPDATE products
            SET stock_threshold = ?,
                updated_at = NOW()
            WHERE id = ?
        """, stockThreshold, id);
    }

    public static class ProductThresholdRow {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer quantity;
        private Integer stockThreshold;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Integer getStockThreshold() { return stockThreshold; }
        public void setStockThreshold(Integer stockThreshold) { this.stockThreshold = stockThreshold; }
    }
}