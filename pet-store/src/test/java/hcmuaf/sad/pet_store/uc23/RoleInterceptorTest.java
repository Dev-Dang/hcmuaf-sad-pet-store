package hcmuaf.sad.pet_store.uc23;

import hcmuaf.sad.pet_store.interceptor.RoleInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

class RoleInterceptorTest {

    private RoleInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RoleInterceptor();
    }

    @Test
    void adminPath_missingSession_shouldRedirectExpiredLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/customers");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/auth/login?expired=true");
    }

    @Test
    void adminPath_customerRole_shouldRedirectForbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/customers");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "KHG-0000001");
        session.setAttribute("role", "CUSTOMER");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getRedirectedUrl()).isEqualTo("/error/403");
    }

    @Test
    void adminPath_adminRole_shouldContinue() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/customers");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userCode", "ADM-0001");
        session.setAttribute("role", "ADMIN");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
        assertThat(response.getRedirectedUrl()).isNull();
    }
}
