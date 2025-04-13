package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.entity.UserAuthCache;
import spring_devjob.repository.UserAuthCacheRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserAuthCacheService {

    private final UserAuthCacheRepository userAuthCacheRepository;

    public void saveUserWithPermission(UserAuthCache userAuthCache){
        userAuthCacheRepository.save(userAuthCache);
    }

}
