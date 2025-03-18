package spring_devjob.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import spring_devjob.dto.response.ApiResponse;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception ex, WebRequest request) {

        log.error("Đã xảy ra lỗi: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(ex.getClass().getSimpleName())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler({AppException.class})
    public ResponseEntity<ErrorResponse> handlingAppException(AppException exception, WebRequest request){
        ErrorCode errorCode = exception.getErrorCode();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(exception.getClass().getSimpleName())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(errorResponse);
    }

    // Lỗi khi Validation thuộc tính
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        List<String> messages = new ArrayList<>();

        // MethodArgumentNotValidException
        if (ex instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) ex).getBindingResult();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            for (FieldError fieldError : fieldErrors) {
                messages.add(fieldError.getDefaultMessage());
            }
            errorResponse.setError("Invalid Payload");
        }
        // ConstraintViolationException
        else if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException violationException = (ConstraintViolationException) ex;
            for (ConstraintViolation<?> violation : violationException.getConstraintViolations()) {
                messages.add(violation.getMessage());
            }
            errorResponse.setError("Invalid Parameter");
        }
        // MissingServletRequestParameterException
        else if (ex instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException missingParamEx = (MissingServletRequestParameterException) ex;
            errorResponse.setError("Invalid Parameter");
            messages.add("Thiếu tham số: " + missingParamEx.getParameterName());
        }

        String errorMessages = String.join(". ", messages);
        errorResponse.setMessage(errorMessages);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Lỗi khi phân quyền
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<ErrorResponse> handingAccessDeniedException(Exception exception, WebRequest request) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.getCode())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Forbidden")
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(errorResponse);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("Invalid Argument")
                .message(exception.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(value = {FileException.class})
    public ResponseEntity<ErrorResponse> handleFileUploadException(Exception ex, WebRequest request) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .error(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Loi chuyen request thanh object Java
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParseException(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("JSON Parse Error: {}", ex.getMessage(), ex);

        String message = "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại giá trị truyền vào.";

        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            Class<?> targetType = invalidFormatException.getTargetType();
            if (targetType.isEnum()) {
                message = "Giá trị '" + invalidFormatException.getValue() + "' không hợp lệ cho " + targetType.getSimpleName() +
                        ". Các giá trị hợp lệ là: " + Arrays.toString(targetType.getEnumConstants());
            } else {
                message = "Giá trị '" + invalidFormatException.getValue() + "' không hợp lệ. Kiểu mong đợi là: " + targetType.getSimpleName();
            }
        }
        else if (ex.getCause() instanceof MismatchedInputException) {
            message = "Dữ liệu đầu vào không đúng kiểu. Vui lòng kiểm tra lại.";
        }
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .timestamp(new Date())
                .path(request.getDescription(false).replace("uri=", ""))
                .error("JSON Parse Error")
                .message(message)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


}
