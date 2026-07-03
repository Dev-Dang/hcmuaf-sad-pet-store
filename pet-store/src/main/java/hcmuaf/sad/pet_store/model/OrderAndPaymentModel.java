package hcmuaf.sad.pet_store.model;

import hcmuaf.sad.pet_store.dto.OrderCheckoutDto;
import hcmuaf.sad.pet_store.dto.PaymentFormDto;
import hcmuaf.sad.pet_store.exception.AppException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.util.EntityType;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class OrderAndPaymentModel {

    public static String createOrder(Long userId, OrderCheckoutDto dto, List<Map<String, Object>> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        double totalAmount = 0;
        for (Map<String, Object> item : cartItems) {
            double price = ((Number) item.get("price")).doubleValue();
            int quantity = ((Number) item.get("quantity")).intValue();
            totalAmount += price * quantity;
        }
        final double finalTotal = totalAmount;

        String orderCode = BusinessKeyGenerator.next(EntityType.ORDER);

        DBUtils.tx().execute(status -> {
            String insertOrderSql = "INSERT INTO orders (order_code, user_id, receiver_name, receiver_phone, shipping_address, place_id, total_amount, status, created_at) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', NOW())";
            DBUtils.jdbc().update(insertOrderSql, orderCode, userId, dto.getReceiverName(), dto.getReceiverPhone(), dto.getShippingAddress(), dto.getPlaceId(), finalTotal);

            String insertDetailSql = "INSERT INTO order_details (order_code, product_id, quantity, price) VALUES (?, ?, ?, ?)";
            String updateStockSql = "UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?";

            for (Map<String, Object> item : cartItems) {
                Long productId = ((Number) item.get("id")).longValue();
                int qty = ((Number) item.get("quantity")).intValue();
                double price = ((Number) item.get("price")).doubleValue();

                DBUtils.jdbc().update(insertDetailSql, orderCode, productId, qty, price);

                int rowsAffected = DBUtils.jdbc().update(updateStockSql, qty, productId, qty);
                if (rowsAffected == 0) {
                    log.error("Sản phẩm ID {} không đáp ứng đủ số lượng tồn kho.", productId);
                    throw new AppException(ErrorCode.BAD_REQUEST);
                }
            }
            return null;
        });

        return orderCode;
    }

    public static void processPayment(PaymentFormDto dto) {
        if ("CREDIT".equals(dto.getPaymentType())) {
            if (dto.getCreditNumber() == null || dto.getCreditNumber().trim().length() < 16) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        } else if ("CHECK".equals(dto.getPaymentType())) {
            if (dto.getCheckName() == null || dto.getCheckBankId() == null) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }
        }

        String paymentCode = BusinessKeyGenerator.next(EntityType.ORDER);

        DBUtils.tx().execute(status -> {
            String insertPaymentSql = "INSERT INTO payments (payment_code, order_code, amount, payment_type, created_at) VALUES (?, ?, ?, ?, NOW())";
            DBUtils.jdbc().update(insertPaymentSql, paymentCode, dto.getOrderCode(), dto.getAmount(), dto.getPaymentType());

            if ("CASH".equals(dto.getPaymentType())) {
                String sql = "INSERT INTO cash_payments (payment_code, cash_tendered) VALUES (?, ?)";
                DBUtils.jdbc().update(sql, paymentCode, dto.getCashTendered());
            } else if ("CHECK".equals(dto.getPaymentType())) {
                String sql = "INSERT INTO check_payments (payment_code, name, bank_id) VALUES (?, ?, ?)";
                DBUtils.jdbc().update(sql, paymentCode, dto.getCheckName(), dto.getCheckBankId());
            } else if ("CREDIT".equals(dto.getPaymentType())) {
                String sql = "INSERT INTO credit_payments (payment_code, card_number, card_type, exp_date) VALUES (?, ?, ?, ?)";
                DBUtils.jdbc().update(sql, paymentCode, dto.getCreditNumber(), dto.getCreditType(), dto.getCreditExpDate());
            }

            String updateOrderSql = "UPDATE orders SET status = 'PAID' WHERE order_code = ?";
            DBUtils.jdbc().update(updateOrderSql, dto.getOrderCode());

            return null;
        });
    }
}