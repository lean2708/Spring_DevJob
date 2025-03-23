package spring_devjob.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import spring_devjob.entity.RedisRevokedToken;
import spring_devjob.repository.RevokedTokenRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisTokenService {

    private final RevokedTokenRepository revokedTokenRepository;

    public void saveRevokedToken(RedisRevokedToken redisRevokedToken){
        revokedTokenRepository.save(redisRevokedToken);
    }
}
