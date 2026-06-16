package hcmuaf.sad.pet_store.uc1;

import hcmuaf.sad.pet_store.model.User;
import hcmuaf.sad.pet_store.model.enums.UserRole;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "uc1"})
class RegisterControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        // MockMvc commit transaction riêng → @Transactional trên test không rollback được
        jdbcTemplate.update("DELETE FROM user_credential");
        jdbcTemplate.update("DELETE FROM users");
    }

    // ── [1.1.2] GET /auth/register ────────────────────────────────────────────

    @Test
    void showForm_shouldReturn200_withRegisterView() throws Exception {
        // [1.1.2] Hiển thị form đăng ký
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));
    }

    @Test
    void showForm_shouldHaveEmptyRegisterRequestInModel() throws Exception {
        // [1.1.2] Hiển thị form đăng ký
        mockMvc.perform(get("/auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    // ── [1.1.4 EF2] Spring Validation ────────────────────────────────────────

    @Test
    void register_blankDisplayName_shouldReturnFormWithFieldError() throws Exception {
        // [1.1.4 EF2] Không tạo tài khoản, hiển thị lỗi
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "")
                        .param("email", "user@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "displayName"));
    }

    @Test
    void register_blankEmail_shouldReturnFormWithFieldError() throws Exception {
        // [1.1.4 EF2] Không tạo tài khoản, hiển thị lỗi
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Test User")
                        .param("email", "")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));
    }

    @Test
    void register_invalidEmailFormat_shouldReturnFormWithFieldError() throws Exception {
        // [1.1.4 EF2] Không tạo tài khoản, hiển thị lỗi
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Test User")
                        .param("email", "not-an-email")
                        .param("password", "pass1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));
    }

    @Test
    void register_blankPassword_shouldReturnFormWithFieldError() throws Exception {
        // [1.1.4 EF2] Không tạo tài khoản, hiển thị lỗi
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Test User")
                        .param("email", "user@test.com")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "password"));
    }

    @Test
    void register_shortPassword_shouldReturnFormWithFieldError() throws Exception {
        // [1.1.4 EF2] Không tạo tài khoản, hiển thị lỗi
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Test User")
                        .param("email", "user@test.com")
                        .param("password", "pass123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "password"));
    }

    // ── [1.1.5 EF1] email đã tồn tại ─────────────────────────────────────────

    @Test
    void register_existingEmail_shouldRenderErrorView() throws Exception {
        // [1.1.5 EF1] Email đã tồn tại → AppException(EMAIL_ALREADY_EXISTS)
        new User(UUID.randomUUID().toString(), "existing@test.com", "Existing User", UserRole.CUSTOMER).insert();

        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Another User")
                        .param("email", "existing@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/error-page"));
    }

    // ── [1.1.6–1.1.8] NF — happy path ────────────────────────────────────────

    @Test
    void register_validData_shouldInsertUserToDb() throws Exception {
        // [1.1.6] Tạo tài khoản Customer mới
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "New User")
                        .param("email", "newuser@test.com")
                        .param("password", "pass1234"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM users WHERE email = ? AND is_current = true", "newuser@test.com");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("display_name")).isEqualTo("New User");
        assertThat(rows.get(0).get("role")).isEqualTo("CUSTOMER");
    }

    @Test
    void register_validData_shouldInsertEmailCredential() throws Exception {
        // [1.1.6] Tạo EMAIL credential
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "New User")
                        .param("email", "credtest@test.com")
                        .param("password", "pass1234"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT uc.* FROM user_credential uc JOIN users u ON uc.user_code = u.user_code WHERE u.email = ? AND u.is_current = true",
                "credtest@test.com");
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("provider")).isEqualTo("EMAIL");
        assertThat(rows.get(0).get("provider_user_id")).isNull();
        assertThat(rows.get(0).get("secret_hash")).isNotNull();
    }

    @Test
    void register_validData_shouldSetSessionAttributes() throws Exception {
        // [1.1.7] Tạo phiên đăng nhập Customer
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .param("displayName", "Session User")
                        .param("email", "session@test.com")
                        .param("password", "pass1234"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute("userCode")).isNotNull();
        assertThat(session.getAttribute("role")).isEqualTo("CUSTOMER");
        assertThat(session.getMaxInactiveInterval()).isEqualTo(86400);
    }

    @Test
    void register_validData_shouldRedirectToHome() throws Exception {
        // [1.1.8] Điều hướng Customer vào trang chủ
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Redirect User")
                        .param("email", "redirect@test.com")
                        .param("password", "pass1234"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void register_emailNormalization_shouldStoreLowercase() throws Exception {
        // email normalize: trim + lowercase trước khi lưu vào DB
        mockMvc.perform(post("/auth/register")
                        .param("displayName", "Normalize User")
                        .param("email", "  UPPER@TEST.COM  ")
                        .param("password", "pass1234"));

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM users WHERE email = ? AND is_current = true", "upper@test.com");
        assertThat(rows).hasSize(1);
    }
}
