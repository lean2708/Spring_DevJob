package spring_devjob.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import spring_devjob.config.VNPAYConfig;
import spring_devjob.constants.PaymentConstants;
import spring_devjob.constants.RoleEnum;
import spring_devjob.dto.request.PaymentCallbackRequest;
import spring_devjob.dto.response.*;
import spring_devjob.entity.*;
import spring_devjob.entity.relationship.SubHasSkill;
import spring_devjob.entity.relationship.UserHasRole;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;
import spring_devjob.mapper.SubscriberMapper;
import spring_devjob.repository.*;
import spring_devjob.repository.relationship.SubHasSkillRepository;
import spring_devjob.repository.relationship.UserHasRoleRepository;
import spring_devjob.service.relationship.SubHasSkillService;
import spring_devjob.util.VNPayUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j(topic = "SUB-SERVICE")
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
    private final SubHasSkillService subHasSkillService;


    public VNPayResponse createVnPayPayment(String premiumType, HttpServletRequest request) {
        Map<String, String> vnpParamsMap = vnPayConfig.getVNPayConfig();

        long amount = 100;

        switch (premiumType) {
            case "1-month":
                amount *= PaymentConstants.ONE_MONTH_PRICE;
                break;
            case "3-month":
                amount *= PaymentConstants.THREE_MONTHS_PRICE;
                break;
            case "6-month":
                amount *= PaymentConstants.SIX_MONTHS_PRICE;
                break;
            case "12-month":
                amount *= PaymentConstants.TWELVE_MONTHS_PRICE;
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

        switch ((int) request.getAmount()) {
            case (int) PaymentConstants.ONE_MONTH_PRICE:
                expiryDate = expiryDate.plusMonths(1);
                break;
            case (int) PaymentConstants.THREE_MONTHS_PRICE:
                expiryDate = expiryDate.plusMonths(3);
                break;
            case (int) PaymentConstants.SIX_MONTHS_PRICE:
                expiryDate = expiryDate.plusMonths(6);
                break;
            case (int) PaymentConstants.TWELVE_MONTHS_PRICE:
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
        Subscriber subscriberDB = findSubById(id);

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
        Subscriber subscriberDB = findSubById(id);

        deactivateSub(subscriberDB);

        subscriberRepository.delete(subscriberDB);
    }

    private void deactivateSub(Subscriber subscriber){
        subHasSkillService.deleteSubHasSkillBySub(subscriber.getId());

        User userDB = userRepository.findByEmail(subscriber.getEmail()).
                orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        roleRepository.findByName(RoleEnum.PRO.name())
                .ifPresent(role -> userHasRoleRepository.deleteByUserAndRole(userDB, role));

        subscriberRepository.delete(subscriber);
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

    public Subscriber findSubById(long id) {
        return subscriberRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_REGISTERED));
    }

    @Scheduled(cron = "0 0 */3 * * *")
    @Async
    public void checkProExpiry() {
        log.info("Update Pro Expiry");

        LocalDate currentDate = LocalDate.now();
        List<Subscriber> subscriberList = subscriberRepository.findAll();

        for (Subscriber subscriber : subscriberList) {
            if(subscriber.getExpiryDate() != null && subscriber.getExpiryDate().isBefore(currentDate)){
                subHasSkillService.deleteSubHasSkillBySub(subscriber.getId());

                User userDB = userRepository.findByEmail(subscriber.getEmail()).
                        orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

                roleRepository.findByName(RoleEnum.PRO.name())
                        .ifPresent(role -> userHasRoleRepository.deleteByUserAndRole(userDB, role));

                subscriberRepository.delete(subscriber);
            }
        }
    }

}
