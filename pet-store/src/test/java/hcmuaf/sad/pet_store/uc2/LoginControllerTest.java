package hcmuaf.sad.pet_store.uc2;

import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.UserCredential;
import hcmuaf.sad.pet_store.model.enums.EntityType;
import hcmuaf.sad.pet_store.model.enums.ProviderType;
import hcmuaf.sad.pet_store.model.enums.UserRole;
import hcmuaf.sad.pet_store.util.BusinessKeyGenerator;
import hcmuaf.sad.pet_store.util.PasswordUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "uc2"})
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM user_credential");
        jdbcTemplate.update("DELETE FROM users");
    }

    // ── [2.1.2] GET /auth/login ──────────────────────────────────────────────

    @Test
    void showLogin_shouldReturn200_withLoginView() throws Exception {
        // [2.1.2] Hiển thị form đăng nhập
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void showLogin_shouldHaveLoginRequestInModel() throws Exception {
        // [2.1.2] Hiển thị form đăng nhập
        mockMvc.perform(get("/auth/login"))
                .andExpect(model().attributeExists("loginRequest"));
    }

    @Test
    void showLogin_alreadyLoggedIn_shouldRedirectToHome() throws Exception {
        // [2.1.2] Nếu đã đăng nhập → redirect /
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "KHG-12345");

        mockMvc.perform(get("/auth/login").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    // ── [2.1.4 EF1] Spring Validation ───────────────────────────────────────

    @Test
    void login_blankEmail_shouldReturnFormWithFieldError() throws Exception {
        // [2.3.1] EF1 — dữ liệu không hợp lệ
        mockMvc.perform(post("/login/email")
                        .param("email", "")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginRequest", "email"));
    }

    @Test
    void login_invalidEmailFormat_shouldReturnFormWithFieldError() throws Exception {
        // [2.3.1] EF1 — dữ liệu không hợp lệ
        mockMvc.perform(post("/login/email")
                        .param("email", "not-an-email")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginRequest", "email"));
    }

    @Test
    void login_blankPassword_shouldReturnFormWithFieldError() throws Exception {
        // [2.3.1] EF1 — dữ liệu không hợp lệ
        mockMvc.perform(post("/login/email")
                        .param("email", "user@test.com")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeHasFieldErrors("loginRequest", "password"));
    }

    // ── [2.1.5 EF2] email/password không đúng ───────────────────────────────

    @Test
    void login_nonExistentEmail_shouldRenderErrorView() throws Exception {
        // [2.4.1] EF2 — Email hoặc mật khẩu không đúng → hiện lỗi inline trên form
        mockMvc.perform(post("/login/email")
                        .param("email", "nonexistent@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void login_wrongPassword_shouldRenderErrorView() throws Exception {
        // [2.4.1] EF2 — Email hoặc mật khẩu không đúng → hiện lỗi inline trên form
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "user@test.com", "Test User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("correct1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "user@test.com")
                        .param("password", "wrong1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void login_userDeletedSoftly_shouldRenderErrorView() throws Exception {
        // [2.4.1] EF2 — User bị xóa mềm không thể đăng nhập → hiện lỗi inline trên form
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "deleted@test.com", "Deleted User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        // Simulate soft delete
        jdbcTemplate.update("UPDATE users SET is_deleted = true WHERE user_code = ?", userCode);

        mockMvc.perform(post("/login/email")
                        .param("email", "deleted@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    // ── [2.1.5–2.1.8] NF — happy path ──────────────────────────────────────

    @Test
    void login_validData_shouldSetSessionAttributes() throws Exception {
        // [2.1.7] Tạo phiên đăng nhập theo role
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "customer@test.com", "Test Customer", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        MvcResult result = mockMvc.perform(post("/login/email")
                        .param("email", "customer@test.com")
                        .param("password", "pass1234"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("userCode")).isEqualTo(userCode);
        assertThat(session.getAttribute("role")).isEqualTo("CUSTOMER");
        assertThat(session.getAttribute("currentUser")).isNotNull();
        assertThat(session.getMaxInactiveInterval()).isEqualTo(86400);
    }

    @Test
    void login_validCustomer_shouldRedirectToHome() throws Exception {
        // [2.1.8] Điều hướng vào trang đích — CUSTOMER → /
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "customer@test.com", "Test Customer", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "customer@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void login_validAdmin_shouldRedirectToAdminHome() throws Exception {
        // [2.1.8] Điều hướng vào trang đích — ADMIN → /admin/
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "admin@test.com", "Test Admin", UserRole.ADMIN).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "admin@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));
    }

    // ── [2.2.1] AF1 — trang đích không phù hợp role ──────────────────────────

    @Test
    void login_customerWithAdminRedirect_shouldRedirectToCustomerHome() throws Exception {
        // [2.2.1] AF1 — trang đích không phù hợp role → dùng mặc định
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "customer@test.com", "Test Customer", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "customer@test.com")
                        .param("password", "pass1234")
                        .param("redirect", "/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void login_adminWithCustomerRedirect_shouldRedirectToAdminHome() throws Exception {
        // [2.2.1] AF1 — trang đích không phù hợp role → dùng mặc định
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "admin@test.com", "Test Admin", UserRole.ADMIN).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "admin@test.com")
                        .param("password", "pass1234")
                        .param("redirect", "/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));
    }

    @Test
    void login_validRedirectPath_shouldRedirectToSpecifiedPath() throws Exception {
        // [2.1.8] AF1 không kích hoạt — trang đích phù hợp role
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "customer@test.com", "Test Customer", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "customer@test.com")
                        .param("password", "pass1234")
                        .param("redirect", "/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));
    }

    // ── email normalization ──────────────────────────────────────────────────

    @Test
    void login_emailNormalization_shouldMatchLowercaseEmail() throws Exception {
        // email normalize: trim + lowercase
        String userCode = BusinessKeyGenerator.next(EntityType.CUSTOMER);
        new User(userCode, "user@test.com", "Test User", UserRole.CUSTOMER).insert();
        new UserCredential(userCode, ProviderType.EMAIL, null, PasswordUtils.hash("pass1234")).insert();

        mockMvc.perform(post("/login/email")
                        .param("email", "  USER@TEST.COM  ")
                        .param("password", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}
