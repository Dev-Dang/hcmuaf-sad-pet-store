package hcmuaf.sad.pet_store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class OrderPageController {

    @GetMapping("/orders")
    public String ordersPage() {
        return "order/orders";
    }

    @GetMapping("/orders/{id}")
    public String orderDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("orderId", id);
        return "order/order-detail";
    }

    @GetMapping("/admin/orders")
    public String adminOrdersPage() {
        return "admin/admin-orders";
    }
}
