package spring_devjob.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import spring_devjob.dto.request.PaymentCallbackRequest;
import spring_devjob.dto.response.*;
import spring_devjob.service.SubscriberService;

import java.util.Set;

@RestController
@RequestMapping("/v1")
@Validated
@RequiredArgsConstructor
@Slf4j
public class SubscriberController {

    private final SubscriberService subscriberService;

    @Operation(summary = "Create VNPay payment URL",
            description = "API này tạo URL thanh toán VNPay dựa trên gói premium mà người dùng chọn. " +
                    "Người dùng có thể chọn một trong các gói: 1-month|3-month|6-month|12-month")
    @PreAuthorize("hasAuthority('CREATE_VNPAY_PAYMENT_URL')")
    @GetMapping("/subscribers/vn-pay")
    public ApiResponse<VNPayResponse> pay(@NotBlank(message = "premiumType không được để trống")
                                              @Pattern(regexp = "^(1-month|3-month|6-month|12-month)$", message = "premiumType chỉ được là: 1-month, 3-month, 6-month, 12-month")
                                              @RequestParam String premiumType, HttpServletRequest request) {
        return new ApiResponse<>(HttpStatus.OK.value(),
                "Tạo thành công URL thanh toán VNPay",
                subscriberService.createVnPayPayment(premiumType, request));
    }

    @Operation(summary = "VNPay payment callback",
            description = "API này xử lý phản hồi từ VNPay sau khi thanh toán. Nếu mã phản hồi là '00', trạng thái tài khoản của người dùng sẽ được cập nhật lên Pro. " +
                    "Nếu mã phản hồi khác, thanh toán sẽ bị coi là thất bại.")
    @PreAuthorize("hasAuthority('VNPAY_PAYMENT_CALLBACK')")
    @PostMapping("/subscribers/vn-pay-callback")
    public ApiResponse<SubscriberResponse> payCallbackHandler(@Valid @RequestBody PaymentCallbackRequest request) {
        String status = request.getResponseCode();
        if (status.equals("00")) {
            return new ApiResponse<>(1000,
                    "Thanh toán thành công",
                    subscriberService.updatePro(request));
        } else {
            log.error("Thanh toán không thành công với mã phản hồi: " + status);
            return new ApiResponse<>(4000, "Thanh toán thất bại", null);
        }
    }

    @PreAuthorize("hasAuthority('CHECK_PRO_STATUS')")
    @GetMapping("/subscribers/pro-status")
    public ApiResponse<SubscriberResponse> checkProStatus(HttpServletRequest request){
        return ApiResponse.<SubscriberResponse>builder()
                .code(HttpStatus.OK.value())
                .result(subscriberService.checkProStatus(request))
                .message("My xPro Status")
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_SUBSCRIBER_BY_ID')")
    @GetMapping("/subscribers/{id}")
    public ApiResponse<SubscriberResponse> fetchById(@PathVariable long id){
        return ApiResponse.<SubscriberResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Fetch Subscriber By Id")
                .result(subscriberService.fetchById(id))
                .build();
    }

    @PreAuthorize("hasAuthority('FETCH_ALL_SUBSCRIBERS')")
    @GetMapping("/subscribers")
    public ApiResponse<PageResponse<SubscriberResponse>> fetchAll(@Min(value = 1, message = "pageNo phải lớn hơn 0")
                                                                      @RequestParam(defaultValue = "1") int pageNo,
                                                            @RequestParam(defaultValue = "10") int pageSize,
                                                                      @RequestParam(required = false) String sortBy){
        return ApiResponse.<PageResponse<SubscriberResponse>>builder()
                .code(HttpStatus.OK.value())
                .result(subscriberService.fetchAllSubscribers(pageNo, pageSize, sortBy))
                .message("Fetch All Subscribers With Pagination")
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_SUBSCRIBER_BY_ID')")
    @DeleteMapping("/subscribers/{id}")
    public ApiResponse<Void> delete(@Min(value = 1, message = "Id phải lớn hơn 0") @PathVariable long id){
        subscriberService.delete(id);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Subscriber By Id")
                .result(null)
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_SUBSCRIBERS')")
    @DeleteMapping("/subscribers")
    public ApiResponse<Void> deleteSubscribers(@Valid @RequestBody @NotEmpty(message = "Danh sách ID không được để trống!")
                                               Set<@Min(value = 1, message = "ID phải lớn hơn 0")Long> ids){
        subscriberService.deleteSubscribers(ids);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Delete Subscribers")
                .result(null)
                .build();
    }

}
