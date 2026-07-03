package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.config.GoogleOAuthConfig;
import hcmuaf.sad.pet_store.dto.auth.EmailCredential;
import hcmuaf.sad.pet_store.dto.auth.GoogleCredential;
import hcmuaf.sad.pet_store.dto.request.LoginRequest;
import hcmuaf.sad.pet_store.exception.BusinessException;
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

    @ModelAttribute
    public void initLoginModel(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        model.addAttribute("googleClientId", GoogleOAuthConfig.getClientId());
    }

    @GetMapping("/auth/login")
    public String showLogin(HttpSession session,
                            Model model,
                            @RequestParam(value = "expired", required = false) boolean expired) {
        // [2.1.1] Truy cập trang đăng nhập hoặc chức năng yêu cầu đăng nhập

        // Redirect nếu user đã đăng nhập
        if (session.getAttribute("userCode") != null) {
            return "redirect:/";
        }

        // [2.1.2 / 3.1.2] Hiển thị form đăng nhập
        if (expired && !model.containsAttribute("error")) {
            model.addAttribute("error", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.");
        }
        return "auth/login";
    }

    @PostMapping("/login/email")
    public String loginEmail(@Valid @ModelAttribute("loginRequest") LoginRequest request,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model,
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
        AuthenticatedUser authUser;
        try {
            authUser = emailAuthProvider.authenticate(credential);
        } catch (BusinessException e) {
            // EF2 - Email hoặc mật khẩu không đúng
            // [2.4.1] Hiển thị thông báo lỗi chung
            model.addAttribute("error", e.getErrorCode().getMessage());
            return "auth/login";
        }

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
            // [2.2.1 / 3.3.1] Đặt lại trang đích về mặc định theo role
            if (isAdminPath != (role == UserRole.ADMIN)) {
                return role == UserRole.ADMIN ? "/admin/" : "/";
            }
            return redirect;
        }
        return role == UserRole.ADMIN ? "/admin/" : "/";
    }

    @GetMapping("/login/google/callback")
    public String googleCallback(
            @RequestParam String credential,
            @RequestParam(value = "redirect", required = false) String redirectUrl,
            HttpSession session,
            Model model) {
        AuthenticatedUser authUser;
        try {
            authUser = googleAuthProvider.authenticate(new GoogleCredential(credential));
        } catch (BusinessException e) {
            // [3.5.1 / 3.6.1] Hiển thị lỗi Đăng nhập Google không thành công
            model.addAttribute("error", e.getErrorCode().getMessage());
            return "auth/login";
        }

        // [3.1.8] Tạo phiên đăng nhập
        establishSession(session, authUser);

        // [3.1.9] Xác định trang đích sau đăng nhập
        String destination = resolveDestination(redirectUrl, authUser.getRole());

        // [3.1.10] Điều hướng Actor đến trang đích
        return "redirect:" + destination;
    }
}
