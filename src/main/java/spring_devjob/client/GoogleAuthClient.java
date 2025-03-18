package spring_devjob.client;

import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import spring_devjob.dto.request.GoogleTokenRequest;
import spring_devjob.dto.response.GoogleTokenResponse;

@FeignClient(name = "google-auth-client", url = "${oauth2.google.google-auth-url}")
public interface GoogleAuthClient {

    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GoogleTokenResponse exchangeToken(@QueryMap GoogleTokenRequest request);
}
