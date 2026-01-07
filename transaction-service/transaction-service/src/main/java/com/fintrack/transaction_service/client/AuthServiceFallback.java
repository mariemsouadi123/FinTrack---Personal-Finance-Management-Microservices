package com.fintrack.transaction_service.client;


import com.fintrack.transaction_service.entities.transaction_service.entities.ValidationRequest;
import com.fintrack.transaction_service.entities.ValidationResponse;

public class AuthServiceFallback implements AuthServiceClient {

    @Override
    public ValidationResponse validateToken(ValidationRequest request) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(false);
        response.setError("Auth service temporarily unavailable");
        return response;
    }

    @Override
    public ValidationResponse validateUser(ValidationRequest request) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(false);
        response.setError("Auth service temporarily unavailable");
        return response;
    }
}
