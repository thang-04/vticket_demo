package com.vticket.vticket.controller;

import com.vticket.vticket.dto.request.AuthenticationRequest;
import com.vticket.vticket.dto.response.ApiResponse;
import com.vticket.vticket.dto.response.AuthenticationResponse;
import com.vticket.vticket.service.LoginService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenController {

    private static final Logger logger = LogManager.getLogger(AuthenController.class);

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        logger.info("Login attempt for username: {}", authenticationRequest.getUsername());
        try {
            var result = loginService.authentication(authenticationRequest);
            logger.info("Login successful for username: {}", authenticationRequest.getUsername());
            return ApiResponse.<AuthenticationResponse>builder()
                    .message("Login successful")
                    .result(result)
                    .build();
        } catch (Exception e) {
            logger.error("Login failed for username: {} - {}", authenticationRequest.getUsername(), e.getMessage(), e);
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(1002)
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }
}
