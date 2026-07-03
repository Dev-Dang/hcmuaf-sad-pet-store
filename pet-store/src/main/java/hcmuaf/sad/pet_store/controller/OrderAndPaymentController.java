package hcmuaf.sad.pet_store.controller;

import hcmuaf.sad.pet_store.dto.OrderCheckoutDto;
import hcmuaf.sad.pet_store.dto.PaymentFormDto;
import hcmuaf.sad.pet_store.exception.AppException;
import hcmuaf.sad.pet_store.model.OrderAndPaymentModel;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderAndPaymentController {

    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        List<Map<String, Object>> cartItems = fetchCartFromSession(session);
        double total = cartItems.stream().mapToDouble(i -> ((Double)i.get("price")) * ((Integer)i.get("quantity"))).sum();

        model.addAttribute("cartDetails", cartItems);
        model.addAttribute("totalAmount", total);
        model.addAttribute("orderCheckoutDto", new OrderCheckoutDto());
        return "order-checkout";
    }

    @PostMapping("/create")
    public String handleCreateOrder(@Valid @ModelAttribute("orderCheckoutDto") OrderCheckoutDto dto,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Map<String, Object>> cartItems = fetchCartFromSession(session);
        double total = cartItems.stream().mapToDouble(i -> ((Double)i.get("price")) * ((Integer)i.get("quantity"))).sum();

        if (bindingResult.hasErrors()) {
            model.addAttribute("cartDetails", cartItems);
            model.addAttribute("totalAmount", total);
            return "order-checkout";
        }

        try {
            String orderCode = OrderAndPaymentModel.createOrder(userId, dto, cartItems);
            session.removeAttribute("cart");
            return "redirect:/order/payment?code=" + orderCode + "&amount=" + total;
        } catch (AppException e) {
            model.addAttribute("cartDetails", cartItems);
            model.addAttribute("totalAmount", total);
            model.addAttribute("errorMessage", "Hệ thống không thể lập đơn hàng do sản phẩm vượt quá số lượng tồn kho.");
            return "order-checkout";
        }
    }

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam("code") String orderCode,
                                  @RequestParam("amount") Double amount,
                                  Model model) {
        PaymentFormDto paymentFormDto = new PaymentFormDto();
        paymentFormDto.setOrderCode(orderCode);
        paymentFormDto.setAmount(amount);

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("payableAmount", amount);
        model.addAttribute("paymentFormDto", paymentFormDto);
        return "payment-execute";
    }

    @PostMapping("/payment/process")
    public String handlePaymentProcess(@Valid @ModelAttribute("paymentFormDto") PaymentFormDto dto,
                                       BindingResult bindingResult,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("orderCode", dto.getOrderCode());
            model.addAttribute("payableAmount", dto.getAmount());
            return "payment-execute";
        }

        try {
            OrderAndPaymentModel.processPayment(dto);
            model.addAttribute("orderCode", dto.getOrderCode());
            return "payment-success";
        } catch (AppException e) {
            model.addAttribute("orderCode", dto.getOrderCode());
            model.addAttribute("payableAmount", dto.getAmount());
            model.addAttribute("errorMessage", "Thực hiện giao dịch thanh toán thất bại. Thông tin thẻ/séc bị từ chối.");
            return "payment-execute";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchCartFromSession(HttpSession session) {
        List<Map<String, Object>> cart = (List<Map<String, Object>>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            Map<String, Object> defaultItem = new HashMap<>();
            defaultItem.put("id", 1L);
            defaultItem.put("itemDescription", "Cát Vệ Sinh Cho Mèo Khử Mùi Premium");
            defaultItem.put("price", 85000.0);
            defaultItem.put("quantity", 2);
            cart.add(defaultItem);
        }
        return cart;
    }
}