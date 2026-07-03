package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.exception.SystemException;
import hcmuaf.sad.pet_store.util.DBUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Temporary UC-23 stub. Replace this file when the official Order model is implemented.
 */
@Getter
@Setter
public class Order {
    private String orderCode;
    private String userCode;
    private String orderStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public static List<Order> findAllByUserCode(String userCode, int page, int size) {
        try {
            int offset = offset(page, size);
            return DBUtils.jdbc().query("""
                    SELECT order_code, user_code, order_status, payment_status, total_amount, created_at
                    FROM orders
                    WHERE user_code = ?
                    ORDER BY created_at DESC
                    LIMIT ? OFFSET ?
                    """, ROW_MAPPER, userCode, size, offset);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static int countByUserCode(String userCode) {
        try {
            Integer count = DBUtils.jdbc().queryForObject(
                    "SELECT COUNT(*) FROM orders WHERE user_code = ?",
                    Integer.class, userCode);
            return count == null ? 0 : count;
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    public static OrderSummary getCustomerOrderSummary(String userCode) {
        try {
            return DBUtils.jdbc().queryForObject("""
                    SELECT COUNT(*) AS order_count, COALESCE(SUM(total_amount), 0) AS total_value
                    FROM orders
                    WHERE user_code = ?
                    """, (rs, rowNum) -> new OrderSummary(
                    rs.getInt("order_count"),
                    rs.getBigDecimal("total_value")
            ), userCode);
        } catch (DataAccessException e) {
            throw new SystemException(ErrorCode.SYSTEM_ERROR, e);
        }
    }

    private static int offset(int page, int size) {
        int safePage = Math.max(page, 1);
        return (safePage - 1) * size;
    }

    public static final RowMapper<Order> ROW_MAPPER = (rs, rowNum) -> {
        Order order = new Order();
        order.setOrderCode(rs.getString("order_code"));
        order.setUserCode(rs.getString("user_code"));
        order.setOrderStatus(rs.getString("order_status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return order;
    };
}
