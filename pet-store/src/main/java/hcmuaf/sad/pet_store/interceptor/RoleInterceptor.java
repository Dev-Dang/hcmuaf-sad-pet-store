package hcmuaf.sad.pet_store.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String userCode = (session != null) ? (String) session.getAttribute("userCode") : null;
        String role = (session != null) ? (String) session.getAttribute("role") : null;
        String path = request.getRequestURI();

        if (path.startsWith("/account/")) {
            // [8.12.1-8.12.2] Redirect khi phiên đăng nhập Customer hết hạn
            if (userCode == null || !"CUSTOMER".equals(role)) {
                response.sendRedirect("/auth/login?expired=true");
                return false;
            }
            return true;
        }

        // Khu vực admin: yêu cầu role ADMIN
        if (!"ADMIN".equals(role)) {
            response.sendRedirect("/error/403");
            return false;
        }

        return true;
    }
}
