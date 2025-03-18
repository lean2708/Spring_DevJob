package spring_devjob.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import spring_devjob.dto.response.GoogleUserResponse;

@FeignClient(name = "google-userInfo-client", url = "${oauth2.google.google-userinfo-url}")
public interface GoogleUserInfoClient {

    @GetMapping(value = "/oauth2/v1/userinfo")
    GoogleUserResponse getUserInfo(@RequestParam("alt") String alt, @RequestParam("access_token") String accessToken);
}
