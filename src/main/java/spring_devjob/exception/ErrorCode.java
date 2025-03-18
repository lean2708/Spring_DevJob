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
    VERIFICATION_CODE_NOT_FOUND(1018, "Mã xác nhận không hợp lệ", HttpStatus.NOT_FOUND),
    VERIFICATION_CODE_EXPIRED(1019, "Mã xác nhận đã hết hạn", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_AMOUNT(1020, "Số tiền thanh toán không đúng hoặc không được hỗ trợ. (30000, 79000, 169000, hoặc 349000)", HttpStatus.BAD_REQUEST),
    USER_NOT_REGISTERED(1021, "Người dùng chưa được đăng ký", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FIELD(1022, "Thuộc tính không hợp lệ để sắp xếp", HttpStatus.BAD_REQUEST),
    INVALID_SORT_FORMAT(1023, "Định dạng sắp xếp không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_OLD_PASSWORD(1025, "Mật khẩu cũ không chính xác", HttpStatus.BAD_REQUEST),
    FORGOT_PASSWORD_TOKEN_NOT_FOUND(1026, "Token đặt lại mật khẩu không tồn tại hoặc đã hết hạn", HttpStatus.NOT_FOUND),
    COMPANY_NOT_FOUND(1027, "Không tìm thấy công ty nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(1028, "Không tìm thấy người dùng nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    SKILL_NOT_FOUND(1029, "Không tìm thấy skill nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    RESUME_NOT_FOUND(1030, "Không tìm thấy resume nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    JOB_NOT_FOUND(1031, "Không tìm thấy job nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    PERMISSION_NOT_FOUND(1032, "Không tìm thấy permission nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(1033, "Không tìm thấy role nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    SUBSCRIBER_NOT_FOUND(1034, "Không tìm thấy subscriber nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    INVALID_REFRESH_TOKEN(1035, "Refresh token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_TYPE_INVALID(1036, "Loại token không hợp lệ", HttpStatus.UNAUTHORIZED),
    EMAIL_EXISTED(1037, "Email đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    PHONE_EXISTED(1037, "Phone đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    REVIEW_NOT_EXISTED(1003, "Review không tồn tại trong hệ thống", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND(1033, "Không tìm thấy review nào với danh sách ID đã cung cấp", HttpStatus.NOT_FOUND),
    JOB_ALREADY_EXISTS_IN_COMPANY(1007, "Job đã tồn tại trong công ty", HttpStatus.BAD_REQUEST),
    COMPANY_MISMATCH(1008, "Company phải trùng với công ty của job hiện tại", HttpStatus.BAD_REQUEST),
    JOB_NOT_SAVED(1009, "Job chưa được user lưu", HttpStatus.BAD_REQUEST),
    RESUME_NOT_SUBMITTED(1009, "Resume chưa được nộp vào Job này", HttpStatus.BAD_REQUEST),
    RESUME_ALREADY_APPLIED(1005, "Resume đã được nộp vào công việc này", HttpStatus.CONFLICT),
    JOB_EXPIRED(1010, "Công việc đã hết hạn, không thể nộp Resume", HttpStatus.BAD_REQUEST)
    ;



    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}
