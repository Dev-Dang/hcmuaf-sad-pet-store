package hcmuaf.sad.pet_store.uc5;

import hcmuaf.sad.pet_store.controller.auth.LoginController;
import hcmuaf.sad.pet_store.controller.auth.LogoutController;
import hcmuaf.sad.pet_store.dto.auth.EmailCredential;
import hcmuaf.sad.pet_store.dto.auth.GoogleCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class LogoutControllerTest {

    private MockMvc logoutMockMvc;
    private MockMvc loginMockMvc;

    @BeforeEach
    void setUp() {
        logoutMockMvc = MockMvcBuilders.standaloneSetup(new LogoutController())
                .build();
        loginMockMvc = MockMvcBuilders.standaloneSetup(new LoginController(
                        (EmailCredential credential) -> null,
                        (GoogleCredential credential) -> null,
                        "test-google-client-id"))
                .build();
    }

    @Test
    void logout_customerSession_shouldInvalidateSessionAndRedirectHome() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "KHG-0000001");
        session.setAttribute("role", "CUSTOMER");

        logoutMockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logout_adminSession_shouldInvalidateSessionAndRedirectLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "ADM-0001");
        session.setAttribute("role", "ADMIN");

        logoutMockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login"));

        assertThat(session.isInvalid()).isTrue();
    }

    @Test
    void logout_missingSession_shouldBeHandledByControllerAndRedirectExpiredLogin() throws Exception {
        logoutMockMvc.perform(post("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?expired=true"));
    }

    @Test
    void logout_missingRole_shouldBeHandledByControllerAndRedirectExpiredLogin() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "KHG-0000001");

        logoutMockMvc.perform(post("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/auth/login?expired=true"));
    }

    @Test
    void showLogin_expiredFlag_shouldRenderExpiredMessage() throws Exception {
        loginMockMvc.perform(get("/auth/login").param("expired", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attribute("googleClientId", "test-google-client-id"))
                .andExpect(model().attribute("error", "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại."));
    }
}
