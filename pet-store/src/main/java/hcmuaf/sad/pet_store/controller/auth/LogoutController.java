package hcmuaf.sad.pet_store.controller.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        // [5.1.2] Kiểm tra phiên đăng nhập hiện tại còn hợp lệ
        HttpSession session = request.getSession(false);
        String role = (session != null)
                ? (String) session.getAttribute("role")
                : null;

        if (session == null || role == null) {
            // [5.2.1] Điều hướng Actor đến trang đăng nhập
            return "redirect:/auth/login?expired=true";
        }

        // [5.1.3] Xác định trang đích sau đăng xuất
        String targetUrl = "ADMIN".equals(role) ? "/auth/login" : "/";

        // [5.1.4] Vô hiệu hóa phiên đăng nhập hiện tại
        session.invalidate();

        // [5.1.5] Điều hướng Actor đến trang đích
        return "redirect:" + targetUrl;
    }
}
