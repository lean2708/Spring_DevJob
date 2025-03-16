package spring_devjob.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import spring_devjob.config.VNPAYConfig;
import spring_devjob.constants.EntityStatus;
import spring_devjob.constants.RoleEnum;
import spring_devjob.dto.request.PaymentCallbackRequest;
import spring_devjob.dto.response.*;
import spring_devjob.entity.*;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.SubscriberMapper;
import spring_devjob.repository.*;
import spring_devjob.util.VNPayUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class SubscriberService {

    private final VNPAYConfig vnPayConfig;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final SubscriberRepository subscriberRepository;
    private final SkillRepository skillRepository;
    private final SubscriberMapper subscriberMapper;
    private final PageableService pageableService;
    private final AuthService authService;
    private final UserHasRoleRepository userHasRoleRepository;
    private final SubHasSkillRepository subHasSkillRepository;


    public VNPayResponse createVnPayPayment(String premiumType, HttpServletRequest request) {
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();

        long amount = 100;

        switch (premiumType) {
            case "1-month":
                amount *=  30000L;
                break;
            case "3-month":
                amount *= 79000L;
                break;
            case "6-month":
                amount *= 169000L;
                break;
            case "12-month":
                amount *= 349000L;
                break;
            default:
                throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        vnpParamsMap.put("vnp_Amount", String.valueOf(amount));

        vnpParamsMap.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        // Tao chuoi da ma hoa
        String queryUrl = VNPayUtil.getPaymentURL(vnpParamsMap, true);

        // Tao chuoi chua ma hoa
        String hashData = VNPayUtil.getPaymentURL(vnpParamsMap, false);
        // Thêm vnp_SecureHash vào URL
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getSecretKey(), hashData);

        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        // Tao URL Final
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;

        return VNPayResponse.builder()
                .code("OK")
                .message("Mã thanh toán đã được tạo thành công. Bạn sẽ được chuyển đến cổng thanh toán để hoàn tất giao dịch.")
                .paymentUrl(paymentUrl).build();
    }

    @Transactional
    public SubscriberResponse updatePro(PaymentCallbackRequest request){
        User user = userRepository.findByEmail(authService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Subscriber subscriber = subscriberRepository.findByEmail(user.getEmail())
                .orElseGet(() -> Subscriber.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .startDate(LocalDate.now())
                        .expiryDate(LocalDate.now())
                        .build());

        if(!CollectionUtils.isEmpty(request.getSkillIds())){
            Set<Skill> skillSet = skillRepository.findAllByIdIn(request.getSkillIds());

            Set<SubHasSkill> subHasSkills = skillSet.stream()
                    .map(skill -> new SubHasSkill(subscriber,skill))
                    .collect(Collectors.toSet());

            subscriber.setSkills(new HashSet<>(subHasSkillRepository.saveAll(subHasSkills)));
        }

        LocalDate expiryDate = subscriber.getExpiryDate();

        switch ((int)  request.getAmount()) {
            case 30000:
                expiryDate = expiryDate.plusMonths(1);
                break;
            case 79000:
                expiryDate = expiryDate.plusMonths(3);
                break;
            case 169000:
                expiryDate = expiryDate.plusMonths(6);
                break;
            case 349000:
                expiryDate = expiryDate.plusYears(1);
                break;
            default:
                throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        subscriber.setExpiryDate(expiryDate);

        subscriberRepository.save(subscriber);

        Role proRole = roleRepository.findByName(RoleEnum.PRO.name())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        userHasRoleRepository.save(new UserHasRole(user, proRole));

        emailService.sendUserEmailWithPayment(subscriber);

        emailService.sendSubscribersEmailJobs();

        return subscriberMapper.toSubscriberResponse(subscriber);
    }

    public SubscriberResponse checkProStatus(HttpServletRequest request){

        Subscriber subscriber = subscriberRepository.findByEmail(authService.getCurrentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_REGISTERED));

        return subscriberMapper.toSubscriberResponse(subscriber);
    }

    public SubscriberResponse fetchById(long id){
        Subscriber subscriberDB = findActiveSubById(id);

        return subscriberMapper.toSubscriberResponse(subscriberDB);
    }

    public PageResponse<SubscriberResponse> fetchAllSubscribers(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Subscriber> subscriberPage = subscriberRepository.findAll(pageable);

        List<SubscriberResponse> responses =  new ArrayList<>();

        subscriberPage.getContent().forEach(subscriber ->
            responses.add(subscriberMapper.toSubscriberResponse(subscriber))
        );

        return PageResponse.<SubscriberResponse>builder()
                .page(subscriberPage.getNumber() + 1)
                .size(subscriberPage.getSize())
                .totalPages(subscriberPage.getTotalPages())
                .totalItems(subscriberPage.getTotalElements())
                .items(responses)
                .build();
    }

    @Transactional
    public void delete(long id){
        Subscriber subscriberDB = findActiveSubById(id);

        deactivateSub(subscriberDB);

        subscriberRepository.delete(subscriberDB);
    }

    private void deactivateSub(Subscriber subscriber){
        if(!CollectionUtils.isEmpty(subscriber.getSkills())){
            subscriber.getSkills().clear();
        }
        subscriber.setState(EntityStatus.INACTIVE);
        subscriber.setDeactivatedAt(LocalDate.now());
    }

    @Transactional
    public void deleteSubscribers(Set<Long> ids) {
        Set<Subscriber> subscriberSet = subscriberRepository.findAllByIdIn(ids);

        if(subscriberSet.isEmpty()){
            throw new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND);
        }
        subscriberSet.forEach(this::deactivateSub);

        subscriberRepository.deleteAllInBatch(subscriberSet);
    }

    private Subscriber findActiveSubById(long id) {
        Subscriber subscriberDB = subscriberRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_REGISTERED));

        if (subscriberDB.getState() == EntityStatus.INACTIVE) {
            throw new AppException(ErrorCode.SUBSCRIBER_ALREADY_DELETED);
        }
        return subscriberDB;
    }

    @Scheduled(cron = "0 0 */3 * * *")
    @Async
    public void checkProExpiry() {
        LocalDate currentDate = LocalDate.now();
        List<Subscriber> subscriberList = subscriberRepository.findAll();

        for (Subscriber subscriber : subscriberList) {
            if(subscriber.getExpiryDate() != null && subscriber.getExpiryDate().isBefore(currentDate)){
                subscriberRepository.delete(subscriber);

                User userDB = userRepository.findByEmail(subscriber.getEmail()).
                        orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

                roleRepository.findByName(RoleEnum.PRO.name()).ifPresent(role ->
                        userHasRoleRepository.deleteByUserAndRole(userDB, role));
            }
        }
    }

}
