package com.vticket.vticket.controller;

import com.vticket.vticket.dto.request.AuthenticationRequest;
import com.vticket.vticket.dto.response.ApiResponse;
import com.vticket.vticket.dto.response.AuthenticationResponse;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.service.JwtService;
import com.vticket.vticket.service.LoginService;
import com.vticket.vticket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenController {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest) {
        try {
         var result = loginService.authentication(authenticationRequest);
            return ApiResponse.<AuthenticationResponse>builder()
                    .message("Login successful")
                    .result(result)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(1002)
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }
}
