package hcmuaf.sad.pet_store.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Lỗi nghiệp vụ không được xử lý tại Controller.
    @ExceptionHandler(AppException.class)
    public ModelAndView handleAppException(AppException ex) {
        log.warn("AppException [{}]", ex.getErrorCode(), ex);
        return errorView(ex.getErrorCode().getMessage());
    }

    // Static resource không tồn tại (favicon, devtools, v.v.) — 404 im lặng, không render template
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResource(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Lỗi không xác định.
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return errorView(ErrorCode.SYSTEM_ERROR.getMessage());
    }

    private ModelAndView errorView(String message) {
        ModelAndView view = new ModelAndView("error/generic");
        view.addObject("errorMessage", message);
        return view;
    }
}
