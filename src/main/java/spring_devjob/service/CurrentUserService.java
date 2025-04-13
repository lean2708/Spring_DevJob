package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import spring_devjob.exception.AppException;
import spring_devjob.exception.ErrorCode;


@RequiredArgsConstructor
@Service
public class CurrentUserService {

    // info tu access token
    public String getCurrentUsername(){
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return authentication.getName(); // email
    }

}
