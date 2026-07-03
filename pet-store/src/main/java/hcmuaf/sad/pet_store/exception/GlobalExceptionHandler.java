package hcmuaf.sad.pet_store.exception;

import hcmuaf.sad.pet_store.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Lỗi nghiệp vụ/hệ thống không được xử lý tại Controller.
    @ExceptionHandler(AppException.class)
    public Object handleAppException(AppException ex, HttpServletRequest request) {
        log.warn("AppException [{}]", ex.getErrorCode(), ex);
        if (isApiRequest(request)) {
            return apiError(ex);
        }
        return errorView(ex.getErrorCode().getMessage());
    }

    // Static resource không tồn tại (favicon, devtools, v.v.) — 404 im lặng, không render template
    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResource(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Lỗi không xác định.
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        if (isApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.SYSTEM_ERROR.name(), ErrorCode.SYSTEM_ERROR.getMessage()));
        }
        return errorView(ErrorCode.SYSTEM_ERROR.getMessage());
    }

    private ModelAndView errorView(String message) {
        ModelAndView view = new ModelAndView("error/generic");
        view.addObject("errorMessage", message);
        return view;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                .success(false)
                .errorCode("VALIDATION_FAILED")
                .message("Validation failed")
                .data(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().startsWith("/api/");
    }

    private ResponseEntity<ApiResponse<Void>> apiError(AppException ex) {
        HttpStatus status = ex.getHttpStatus() != null ? HttpStatus.valueOf(ex.getHttpStatus()) : defaultStatus(ex);
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getErrorCode().name(), ex.getErrorCode().getMessage()));
    }

    private HttpStatus defaultStatus(AppException ex) {
        if (ex instanceof BusinessException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof SystemException) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
