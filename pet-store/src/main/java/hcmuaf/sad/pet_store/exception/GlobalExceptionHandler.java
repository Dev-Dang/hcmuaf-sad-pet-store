package hcmuaf.sad.pet_store.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Lỗi nghiệp vụ có kiểm soát — lưu message vào session, redirect sang GET /error (PRG)
    // [1.3.1 / 2.4.1] Hiển thị thông báo lỗi nghiệp vụ tương ứng
    @ExceptionHandler(AppException.class)
    public String handleAppException(AppException ex, HttpServletRequest request) {
        log.warn("AppException [{}]: {}", ex.getErrorCode(), ex.getErrorCode().getMessage());
        request.getSession().setAttribute("errorMessage", ex.getErrorCode().getMessage());
        return "redirect:/error-page";
    }

    // Static resource không tồn tại (favicon, devtools, v.v.) — 404 im lặng, không render template
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResource(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Lỗi không xác định — log để debug, lưu message vào session, redirect sang GET /error
    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        request.getSession().setAttribute("errorMessage", ErrorCode.SYSTEM_ERROR.getMessage());
        return "redirect:/error-page";
    }
}
