package com.fintrack.transaction_service.client;


import com.fintrack.transaction_service.config.FeignConfig;
import com.fintrack.transaction_service.entities.transaction_service.entities.ValidationRequest;
import com.fintrack.transaction_service.entities.ValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "auth-service",
        url = "${feign.client.auth-service.url:}",
        fallback = AuthServiceFallback.class,
        configuration = FeignConfig.class
)
public interface AuthServiceClient {

    @PostMapping("/api/auth/validate")
    ValidationResponse validateToken(@RequestBody ValidationRequest request);

    @PostMapping("/api/auth/validate-user")
    ValidationResponse validateUser(@RequestBody ValidationRequest request);
}
