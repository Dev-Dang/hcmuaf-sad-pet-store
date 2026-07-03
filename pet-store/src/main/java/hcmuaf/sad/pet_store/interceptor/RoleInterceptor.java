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

        // Khu vực admin: phiên hết hạn thì quay lại đăng nhập, sai role thì chặn quyền.
        if (userCode == null || role == null) {
            // [23.7.1+23.7.2] Hiển thị thông báo hết hạn phiên, điều hướng đăng nhập
            response.sendRedirect("/auth/login?expired=true");
            return false;
        }

        if (!"ADMIN".equals(role)) {
            response.sendRedirect("/error/403");
            return false;
        }

        return true;
    }
}
