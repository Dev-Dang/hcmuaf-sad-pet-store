package hcmuaf.sad.pet_store.controller.customer;

import hcmuaf.sad.pet_store.exception.BusinessException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.mapper.CustomerMapper;
import hcmuaf.sad.pet_store.model.Order;
import hcmuaf.sad.pet_store.model.OrderSummary;
import hcmuaf.sad.pet_store.model.ShippingAddress;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {
    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String keyword,
                                @RequestParam(defaultValue = "1") int page,
                                Model model) {
        int safePage = normalizePage(page);
        String normalizedKeyword = normalizeKeyword(keyword);

        List<User> customers;
        int totalItems;
        if (normalizedKeyword == null) {
            // [23.1.2] Truy xuất danh sách tài khoản Customer
            customers = User.findActiveCustomers(safePage, PAGE_SIZE);
            totalItems = User.countActiveCustomers();
        } else {
            // [23.2.2] Tìm tài khoản Customer khớp với từ khóa
            customers = User.searchActiveCustomers(normalizedKeyword, safePage, PAGE_SIZE);
            totalItems = User.countActiveCustomersByKeyword(normalizedKeyword);
        }

        if (normalizedKeyword != null && customers.isEmpty()) {
            // [23.5.1] Hiển thị thông báo không tìm thấy Customer khớp từ khóa
            model.addAttribute("emptyResult", true);
        }

        // [23.1.3+23.2.3] Hiển thị danh sách Customer, có phân trang
        model.addAttribute("customers", CustomerMapper.toCustomerListDto(customers));
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", safePage);
        model.addAttribute("totalPages", totalPages(totalItems));
        return "admin/customer/list";
    }

    @GetMapping("/{userCode}")
    public String customerDetail(@PathVariable String userCode, Model model) {
        // [23.1.5] Truy xuất thông tin chi tiết Customer
        User customer = User.findActiveByUserCode(userCode);
        if (customer == null || customer.getRole() != UserRole.CUSTOMER) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }

        // [23.1.5] Lấy SĐT và địa chỉ mặc định của Customer
        ShippingAddress defaultAddress = ShippingAddress.findDefaultByUserCode(userCode);

        // [23.1.5] Tổng hợp tổng số đơn và tổng giá trị đơn hàng
        OrderSummary summary = Order.getCustomerOrderSummary(userCode);

        // [23.1.6] Hiển thị thông tin chi tiết Customer
        model.addAttribute("customer", CustomerMapper.toCustomerDetailDto(customer, defaultAddress, summary));
        return "admin/customer/detail";
    }

    @GetMapping("/{userCode}/orders")
    public String customerOrders(@PathVariable String userCode,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model) {
        int safePage = normalizePage(page);

        // [23.3.2] Truy xuất danh sách đơn hàng của Customer
        List<Order> orders = Order.findAllByUserCode(userCode, safePage, PAGE_SIZE);

        if (orders.isEmpty()) {
            // [23.6.1] Hiển thị thông báo Customer chưa có đơn hàng
            model.addAttribute("emptyResult", true);
        }

        // [23.3.3] Hiển thị lịch sử đơn hàng, có phân trang
        model.addAttribute("orders", CustomerMapper.toOrderHistoryDto(orders));
        model.addAttribute("userCode", userCode);
        model.addAttribute("page", safePage);
        model.addAttribute("totalPages", totalPages(Order.countByUserCode(userCode)));
        return "admin/customer/orders";
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }

    private int normalizePage(int page) {
        return Math.max(page, 1);
    }

    private int totalPages(int totalItems) {
        return Math.max(1, (int) Math.ceil((double) totalItems / PAGE_SIZE));
    }
}
