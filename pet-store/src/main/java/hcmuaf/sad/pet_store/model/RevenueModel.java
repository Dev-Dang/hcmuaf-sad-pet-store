package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.util.DBUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class RevenueModel {

    private static final String REVENUE_CONDITION = """
        deleted_at IS NULL
        AND order_status IN ('CONFIRMED', 'COMPLETED')
        AND payment_status = 'PAID'
    """;

    public RevenueSummary getSummary() {
        return DBUtils.jdbc().queryForObject("""
            SELECT
                COUNT(*) AS total_orders,
                COALESCE(SUM(total_amount), 0) AS total_revenue,
                COALESCE(SUM(subtotal), 0) AS subtotal,
                COALESCE(SUM(shipping_fee), 0) AS shipping_fee
            FROM orders
            WHERE deleted_at IS NULL
              AND order_status IN ('CONFIRMED', 'COMPLETED')
              AND payment_status = 'PAID'
        """, (rs, row) -> {
            RevenueSummary s = new RevenueSummary();
            s.setTotalOrders(rs.getInt("total_orders"));
            s.setTotalRevenue(rs.getBigDecimal("total_revenue"));
            s.setSubtotal(rs.getBigDecimal("subtotal"));
            s.setShippingFee(rs.getBigDecimal("shipping_fee"));
            return s;
        });
    }

    public List<RevenueOrderRow> findPaidOrders() {
        return DBUtils.jdbc().query("""
            SELECT id, order_code, recipient_name, order_status,
                   payment_status, subtotal, shipping_fee, total_amount, created_at
            FROM orders
            WHERE deleted_at IS NULL
              AND order_status IN ('CONFIRMED', 'COMPLETED')
              AND payment_status = 'PAID'
            ORDER BY created_at DESC
        """, (rs, row) -> {
            RevenueOrderRow o = new RevenueOrderRow();
            o.setId(rs.getLong("id"));
            o.setOrderCode(rs.getString("order_code"));
            o.setRecipientName(rs.getString("recipient_name"));
            o.setOrderStatus(rs.getString("order_status"));
            o.setPaymentStatus(rs.getString("payment_status"));
            o.setSubtotal(rs.getBigDecimal("subtotal"));
            o.setShippingFee(rs.getBigDecimal("shipping_fee"));
            o.setTotalAmount(rs.getBigDecimal("total_amount"));
            o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return o;
        });
    }

    public List<RevenueStatRow> getDailyRevenue() {
        return DBUtils.jdbc().query("""
            SELECT
                DATE_FORMAT(created_at, '%d/%m/%Y') AS label,
                COUNT(*) AS total_orders,
                COALESCE(SUM(total_amount), 0) AS total_revenue
            FROM orders
            WHERE deleted_at IS NULL
              AND order_status IN ('CONFIRMED', 'COMPLETED')
              AND payment_status = 'PAID'
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at) DESC
        """, (rs, row) -> mapStat(rs));
    }

    public List<RevenueStatRow> getMonthlyRevenue() {
        return DBUtils.jdbc().query("""
            SELECT
                DATE_FORMAT(created_at, '%m/%Y') AS label,
                COUNT(*) AS total_orders,
                COALESCE(SUM(total_amount), 0) AS total_revenue
            FROM orders
            WHERE deleted_at IS NULL
              AND order_status IN ('CONFIRMED', 'COMPLETED')
              AND payment_status = 'PAID'
            GROUP BY YEAR(created_at), MONTH(created_at)
            ORDER BY YEAR(created_at) DESC, MONTH(created_at) DESC
        """, (rs, row) -> mapStat(rs));
    }

    public List<RevenueStatRow> getYearlyRevenue() {
        return DBUtils.jdbc().query("""
            SELECT
                YEAR(created_at) AS label,
                COUNT(*) AS total_orders,
                COALESCE(SUM(total_amount), 0) AS total_revenue
            FROM orders
            WHERE deleted_at IS NULL
              AND order_status IN ('CONFIRMED', 'COMPLETED')
              AND payment_status = 'PAID'
            GROUP BY YEAR(created_at)
            ORDER BY YEAR(created_at) DESC
        """, (rs, row) -> mapStat(rs));
    }

    private RevenueStatRow mapStat(ResultSet rs) throws SQLException {
        RevenueStatRow r = new RevenueStatRow();
        r.setLabel(rs.getString("label"));
        r.setTotalOrders(rs.getInt("total_orders"));
        r.setTotalRevenue(rs.getBigDecimal("total_revenue"));
        return r;
    }

    public static class RevenueSummary {
        private int totalOrders;
        private BigDecimal totalRevenue;
        private BigDecimal subtotal;
        private BigDecimal shippingFee;

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    }

    public static class RevenueStatRow {
        private String label;
        private int totalOrders;
        private BigDecimal totalRevenue;

        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }

        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class RevenueOrderRow {
        private Long id;
        private String orderCode;
        private String recipientName;
        private String orderStatus;
        private String paymentStatus;
        private BigDecimal subtotal;
        private BigDecimal shippingFee;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getOrderCode() { return orderCode; }
        public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

        public String getRecipientName() { return recipientName; }
        public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

        public String getOrderStatus() { return orderStatus; }
        public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

        public BigDecimal getShippingFee() { return shippingFee; }
        public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}