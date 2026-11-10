package com.team6.quanlyvemaybay.exception;

import com.team6.quanlyvemaybay.dto.common.ErrorDetail;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.util.List;

/**
 * Bộ xử lý ngoại lệ tập trung trả JSON lỗi theo chuẩn RFC 9457 (application/problem+json)
 * kèm hai trường mở rộng cố định: code và details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_TYPE_PREFIX = "https://api.quanlyvemaybay.vn/errors/";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildProblemResponse(
                HttpStatus.NOT_FOUND,
                ex.getCode(),
                "Không tìm thấy dữ liệu",
                ex.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        return buildProblemResponse(
                ex.getStatus(),
                ex.getCode(),
                ex.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request,
                ex.getDetails()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ErrorDetail(
                        err.getField(),
                        "INVALID_FIELD",
                        err.getDefaultMessage() != null ? err.getDefaultMessage() : "Giá trị không hợp lệ"
                ))
                .toList();

        return buildProblemResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Dữ liệu đầu vào không hợp lệ",
                "Một hoặc nhiều trường dữ liệu không đạt điều kiện kiểm tra.",
                request,
                details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String path = violation.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new ErrorDetail(field, "INVALID_PARAMETER", violation.getMessage());
                })
                .toList();

        return buildProblemResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Tham số truy vấn không hợp lệ",
                "Tham số đầu vào không đáp ứng ràng buộc hợp lệ.",
                request,
                details
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidation(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = ex.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String paramName = result.getMethodParameter().getParameterName();
                    String field = paramName != null ? paramName : "parameter";
                    return result.getResolvableErrors().stream()
                            .map(err -> new ErrorDetail(
                                    field,
                                    "INVALID_PARAMETER",
                                    err.getDefaultMessage() != null ? err.getDefaultMessage() : "Tham số không hợp lệ"
                            ));
                })
                .toList();

        return buildProblemResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Tham số truy vấn không hợp lệ",
                "Tham số đầu vào không đáp ứng ràng buộc hợp lệ.",
                request,
                details
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = List.of(new ErrorDetail(
                ex.getName(),
                "TYPE_MISMATCH",
                "Kiểu dữ liệu của tham số '" + ex.getName() + "' không đúng định dạng"
        ));

        return buildProblemResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER",
                "Tham số không đúng định dạng",
                "Tham số '" + ex.getName() + "' có giá trị không hợp lệ.",
                request,
                details
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        List<ErrorDetail> details = List.of(new ErrorDetail(
                ex.getParameterName(),
                "MISSING_PARAMETER",
                "Thiếu tham số bắt buộc '" + ex.getParameterName() + "'"
        ));

        return buildProblemResponse(
                HttpStatus.BAD_REQUEST,
                "MISSING_PARAMETER",
                "Thiếu tham số bắt buộc",
                ex.getMessage(),
                request,
                details
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        return buildProblemResponse(
                HttpStatus.NOT_FOUND,
                "ENDPOINT_NOT_FOUND",
                "Đường dẫn không tồn tại",
                "Không tìm thấy endpoint hoặc tài nguyên: " + request.getRequestURI(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ProblemDetail> handleDataAccessException(
            DataAccessException ex,
            HttpServletRequest request
    ) {
        log.error("Lỗi truy xuất cơ sở dữ liệu tại {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildProblemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "DATABASE_ERROR",
                "Lỗi cơ sở dữ liệu",
                "Không thể truy xuất dữ liệu từ hệ thống, vui lòng thử lại sau.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Lỗi hệ thống không xác định tại {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildProblemResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Lỗi hệ thống",
                "Đã xảy ra lỗi ngoài ý muốn, vui lòng thử lại sau.",
                request,
                List.of()
        );
    }

    private ResponseEntity<ProblemDetail> buildProblemResponse(
            HttpStatus status,
            String code,
            String title,
            String detail,
            HttpServletRequest request,
            List<ErrorDetail> details
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(PROBLEM_TYPE_PREFIX + code.toLowerCase().replace('_', '-')));
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", code);
        problem.setProperty("details", details == null ? List.of() : details);

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
