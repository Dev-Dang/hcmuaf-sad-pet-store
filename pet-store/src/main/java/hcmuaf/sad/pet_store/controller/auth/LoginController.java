package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.dto.auth.EmailCredential;
import hcmuaf.sad.pet_store.dto.auth.GoogleCredential;
import hcmuaf.sad.pet_store.dto.request.LoginRequest;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.model.policy.SessionPolicy;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final AuthProvider<EmailCredential> emailAuthProvider;
    private final AuthProvider<GoogleCredential> googleAuthProvider;

    public LoginController() {
        this.emailAuthProvider = new EmailAuthProvider();
        this.googleAuthProvider = new GoogleAuthProvider();
    }

    @GetMapping("/auth/login")
    public String showLogin(HttpSession session, Model model) {
        // [2.1.1] Truy cập trang đăng nhập hoặc chức năng yêu cầu đăng nhập

        // Redirect nếu user đã đăng nhập
        if (session.getAttribute("userCode") != null) {
            return "redirect:/";
        }

        // [2.1.2] Hiển thị form đăng nhập
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }

    @PostMapping("/login/email")
    public String loginEmail(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                             BindingResult bindingResult,
                             HttpSession session,
                             @RequestParam(value = "redirect", required = false) String redirectUrl) {

        // [2.1.3] Nhập email, mật khẩu và gửi yêu cầu đăng nhập

        // [2.1.4] Kiểm tra tính hợp lệ của dữ liệu đầu vào

        // EF1 - Dữ liệu không hợp lệ
        // [2.3.1] Hiển thị lỗi dữ liệu không hợp lệ
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        // [2.1.5] Kiểm tra email và mật khẩu với tài khoản trong hệ thống
        EmailCredential credential = new EmailCredential(request.getEmail(), request.getPassword());
        AuthenticatedUser authUser = emailAuthProvider.authenticate(credential);

        // [2.1.7] Tạo phiên đăng nhập theo role
        establishSession(session, authUser);

        // [2.1.8] Xác định trang đích sau đăng nhập theo role
        String destination = resolveDestination(redirectUrl, authUser.getRole());

        // [2.1.9] Điều hướng Actor đến trang đích
        return "redirect:" + destination;
    }

    private void establishSession(HttpSession session, AuthenticatedUser authUser) {
        session.setAttribute("userCode", authUser.getUserCode());
        session.setAttribute("role", authUser.getRole().name());
        session.setAttribute("currentUser", authUser);
        session.setMaxInactiveInterval(SessionPolicy.SESSION_TTL_SECONDS);
    }

    private String resolveDestination(String redirect, UserRole role) {
        if (redirect != null && !redirect.isBlank()) {
            boolean isAdminPath = redirect.startsWith("/admin");
            // AF1 — trang đích không phù hợp role
            // [2.2.1] Đặt lại trang đích về mặc định theo role
            if (isAdminPath != (role == UserRole.ADMIN)) {
                return role == UserRole.ADMIN ? "/admin/" : "/";
            }
            return redirect;
        }
        return role == UserRole.ADMIN ? "/admin/" : "/";
    }

    @GetMapping("/login/google/callback")
    public String googleCallback() {
        // TODO: UC-3
        throw new UnsupportedOperationException("TODO: UC-3");
    }
}
