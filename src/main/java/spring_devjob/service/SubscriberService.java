package spring_devjob.service;

import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.util.StringUtils;
import spring_devjob.config.VNPAYConfig;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public SubscriberResponse updatePro(PaymentCallbackRequest request){
        User user = userRepository.findByEmail(authService.getCurrentUsername()).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Subscriber subscriber = Subscriber.builder()
                .name(user.getName())
                .email(user.getEmail())
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now())
                .build();

        if(subscriberRepository.existsByEmail(user.getEmail())){
            subscriber = subscriberRepository.findByEmail(user.getEmail());
        }

        if(request.getSkills() != null && !request.getSkills().isEmpty()){
            List<Skill> skills = skillRepository.findAllByNameIn(request.getSkills());
            subscriber.setSkills(skills);
        }

        LocalDate expiryDate = subscriber.getExpiryDate();

        long amount = request.getAmount();

        switch ((int) amount) {
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

        Role proRole = roleRepository.findByName(RoleEnum.PRO.name()).orElseThrow(
                () -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        List<Role> roles = user.getRoles();
        roles.add(proRole);
        user.setRoles(roles);

        userRepository.save(user);

        emailService.sendUserEmailWithPayment(subscriber);

        List<String> nameSkillsList = new ArrayList<>();
        for(Skill skill : subscriber.getSkills()){
            nameSkillsList.add(skill.getName());
        }

        SubscriberResponse response = subscriberMapper.toSubscriberResponse(subscriber);
        response.setSkills(nameSkillsList);

        emailService.sendSubscribersEmailJobs();

        return response;
    }

    public SubscriberResponse checkProStatus(HttpServletRequest request){

        Subscriber subscriber = subscriberRepository.findByEmail(authService.getCurrentUsername());
        if(subscriber == null){
            throw new AppException(ErrorCode.USER_NOT_REGISTERED);
        }

        List<String> nameSkillsList = new ArrayList<>();
        for(Skill skill : subscriber.getSkills()){
            nameSkillsList.add(skill.getName());
        }

        SubscriberResponse response = subscriberMapper.toSubscriberResponse(subscriber);
        response.setSkills(nameSkillsList);

        return response;
    }

    public SubscriberResponse fetchById(long id){
        Subscriber subscriberDB = subscriberRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_REGISTERED));

        List<String> nameSkillsList = new ArrayList<>();
        for(Skill skill : subscriberDB.getSkills()){
            nameSkillsList.add(skill.getName());
        }

        SubscriberResponse response = subscriberMapper.toSubscriberResponse(subscriberDB);
        response.setSkills(nameSkillsList);

        return response;
    }

    public PageResponse<SubscriberResponse> fetchAllSubscribers(int pageNo, int pageSize, String sortBy){
        pageNo = pageNo - 1;

        Pageable pageable = pageableService.createPageable(pageNo, pageSize, sortBy);

        Page<Subscriber> subscriberPage = subscriberRepository.findAll(pageable);

        List<SubscriberResponse> responses =  new ArrayList<>();
        for(Subscriber subscriber : subscriberPage.getContent()){
            List<String> nameSkillsList = new ArrayList<>();
            for(Skill skill : subscriber.getSkills()){
                nameSkillsList.add(skill.getName());
            }
            SubscriberResponse subscriberResponse = subscriberMapper.toSubscriberResponse(subscriber);
            subscriberResponse.setSkills(nameSkillsList);

            responses.add(subscriberResponse);
        }

        return PageResponse.<SubscriberResponse>builder()
                .page(subscriberPage.getNumber() + 1)
                .size(subscriberPage.getSize())
                .totalPages(subscriberPage.getTotalPages())
                .totalItems(subscriberPage.getTotalElements())
                .items(responses)
                .build();
    }

    public void delete(long id){
        Subscriber subscriberDB = subscriberRepository.findById(id)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_REGISTERED));

        subscriberRepository.delete(subscriberDB);
    }

    public void deleteSubscribers(List<Long> ids) {
        List<Subscriber> subscriberList = subscriberRepository.findAllByIdIn(ids);

        if(subscriberList.isEmpty()){
            throw new AppException(ErrorCode.SUBSCRIBER_NOT_FOUND);
        }

        subscriberRepository.deleteAllInBatch(subscriberList);
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

                userDB.getRoles().removeIf(role -> role.getName().equals("PRO"));

                userRepository.save(userDB);
            }
        }
    }

}
