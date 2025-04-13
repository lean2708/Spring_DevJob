package spring_devjob.service;

import jakarta.servlet.http.HttpServletRequest;
import spring_devjob.dto.request.PaymentCallbackRequest;
import spring_devjob.dto.response.PageResponse;
import spring_devjob.dto.response.SubscriberResponse;
import spring_devjob.dto.response.VNPayResponse;
import spring_devjob.entity.Subscriber;

import java.util.Set;

public interface SubscriberService {

    VNPayResponse createVnPayPayment(String premiumType, HttpServletRequest request);

    SubscriberResponse updatePro(PaymentCallbackRequest request);

    SubscriberResponse checkProStatus(HttpServletRequest request);

    SubscriberResponse fetchById(long id);

    PageResponse<SubscriberResponse> fetchAllSubscribers(int pageNo, int pageSize, String sortBy);

    void delete(long id);

    void deleteSubscribers(Set<Long> ids);


}
