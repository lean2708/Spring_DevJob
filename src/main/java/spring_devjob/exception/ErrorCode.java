package spring_devjob.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1000, "Chưa xác thực người dùng", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1001, "Bạn chưa được phân quyền truy cập", HttpStatus.FORBIDDEN),
    USER_EXISTED(1002, "User đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "Người dùng không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(1004, "Mật khẩu không chính xác", HttpStatus.UNAUTHORIZED),
    COMPANY_EXISTED(1005, "Company đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_EXISTED(1006, "Company không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    JOB_EXISTED(1007, "Job đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    JOB_NOT_EXISTED(1008, "Job không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    RESUME_EXISTED(1009, "Resume đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    RESUME_NOT_EXISTED(1010, "Resume không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    SKILL_EXISTED(1011, "Skill đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    SKILL_NOT_EXISTED(1012, "Skill không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1013, "Role đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(1014, "Role không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    PERMISSION_EXISTED(1015, "Permission đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_EXISTED(1016, "Permission không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    EMAIL_SEND_FAILED(1017, "Lỗi khi gửi email", HttpStatus.BAD_REQUEST),
    VERIFICATION_CODE_NOT_FOUND(1018, "Mã xác nhận không tồn tại", HttpStatus.NOT_FOUND),
    VERIFICATION_CODE_EXPIRED(1019, "Mã xác nhận đã hết hạn", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_AMOUNT(1020, "Số tiền thanh toán không đúng hoặc không được hỗ trợ. (30000, 79000, 169000, hoặc 349000)", HttpStatus.BAD_REQUEST),
    USER_NOT_REGISTERED(1021, "Người dùng chưa được đăng ký", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FIELD(1022, "Thuộc tính không hợp lệ để sắp xếp", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FORMAT(1023, "Định dạng sắp xếp không hợp lệ", HttpStatus.BAD_REQUEST),
    COMPANY_NOT_ASSOCIATED(1024, "Người dùng không thuộc công ty nào để xem danh sách công việc", HttpStatus.FORBIDDEN)
    ;



    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}
