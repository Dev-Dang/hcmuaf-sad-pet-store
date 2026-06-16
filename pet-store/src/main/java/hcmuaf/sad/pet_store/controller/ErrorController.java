package hcmuaf.sad.pet_store.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    // Nhận errorMessage từ session (đặt bởi GlobalExceptionHandler), xóa sau khi đọc — PRG pattern
    // Nếu không có message (reload lần 2 hoặc truy cập trực tiếp) → về trang chủ
    @GetMapping("/error-page")
    public String error(HttpSession session, Model model) {
        String message = (String) session.getAttribute("errorMessage");
        session.removeAttribute("errorMessage");
        if (message == null) {
            return "redirect:/";
        }
        model.addAttribute("errorMessage", message);
        return "error/generic";
    }
}
