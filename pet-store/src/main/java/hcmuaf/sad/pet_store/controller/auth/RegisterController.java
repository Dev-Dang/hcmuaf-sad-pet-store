package hcmuaf.sad.pet_store.controller.auth;

import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.DBUtils;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import hcmuaf.sad.pet_store.dto.request.RegisterRequest;
import hcmuaf.sad.pet_store.exception.AppException;
import hcmuaf.sad.pet_store.exception.ErrorCode;
import hcmuaf.sad.pet_store.mapper.UserMapper;
import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
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

@Controller
public class RegisterController {

    @GetMapping("/auth/register")
    public String showForm(HttpSession session, Model model) {
        // Đã đăng nhập → về trang chủ
        if (session.getAttribute("userCode") != null) {
            return "redirect:/";
        }
        model.addAttribute("registerRequest", new RegisterRequest());
        // [1.1.2] Hiển thị form đăng ký
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult result,
                           HttpSession session,
                           Model model) {

        // [1.1.4] Kiểm tra tính hợp lệ của thông tin đăng ký

        // [1.2.1] Không tạo tài khoản, hiển thị lỗi
        if (result.hasErrors()) {
            // EF1 - Thông tin đăng ký không hợp lệ
            return "auth/register";
        }

        // [1.1.5] Kiểm tra email chưa tồn tại trong hệ thống
        if (User.existsByEmail(request.getEmail())) {
            // EF2 — Email đã tồn tại trong hệ thống
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Chuẩn bị dữ liệu tài khoản mới
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        String secretHash = PasswordUtils.hash(request.getPassword());

        // [1.1.6] Tạo tài khoản Customer mới (và EMAIL credential tương ứng)
        User newUser = new User(userCode, request.getEmail(), request.getDisplayName(), UserRole.CUSTOMER);
        UserCredential newCredential = new UserCredential(userCode, ProviderType.EMAIL, null, secretHash);

        // Transaction
        DBUtils.tx().executeWithoutResult(status -> {
            newUser.insert();
            newCredential.insert();
        });

        // [1.1.7] Tạo phiên đăng nhập Customer
        session.setAttribute("userCode", userCode);
        session.setAttribute("role", UserRole.CUSTOMER.name());
        session.setAttribute("currentUser", UserMapper.toAuthenticatedUser(newUser));
        session.setMaxInactiveInterval(SessionPolicy.SESSION_TTL_SECONDS);

        // [1.1.8] Điều hướng Customer vào trang chủ
        return "redirect:/";
    }
}
