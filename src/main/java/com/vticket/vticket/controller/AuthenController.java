package com.vticket.vticket.controller;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.AuthenticationRequest;
import com.vticket.vticket.dto.request.IntrospectRequest;
import com.vticket.vticket.dto.request.OtpVerifyRequest;
import com.vticket.vticket.dto.request.RefreshRequest;
import com.vticket.vticket.dto.response.AuthenticationResponse;
import com.vticket.vticket.dto.response.IntrospectResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.JwtService;
import com.vticket.vticket.service.LoginService;
import com.vticket.vticket.service.RegistrationService;
import com.vticket.vticket.service.UserService;
import com.vticket.vticket.utils.ResponseJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
public class AuthenController {

    private static final Logger logger = LogManager.getLogger(AuthenController.class);

    @Autowired
    private LoginService loginService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RegistrationService registrationService;

    @PostMapping("/login")
    public String login(@RequestBody AuthenticationRequest authenticationRequest) {
        logger.info("Login attempt for username: {}", authenticationRequest.getUsername());
        try {
            var result = loginService.authentication(authenticationRequest);
            logger.info("Login successful for username: {}", authenticationRequest.getUsername());
            return ResponseJson.success("Login successful", result);
        } catch (Exception e) {
            logger.error("Login failed for username: {} - {}", authenticationRequest.getUsername(), e.getMessage(), e);
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, e.getMessage());
        }
    }

    @PostMapping("log")

    @PostMapping("/verify-otp")
    public String register(@RequestBody OtpVerifyRequest request) {
        try {
            boolean isVerified = registrationService.verifyOtp(request);
            if (isVerified) {
                return ResponseJson.success("OTP verification successful", null);
            } else {
                return ResponseJson.of(ErrorCode.INVALID_OTP, "Invalid OTP");
            }
        } catch (Exception e) {
            return ResponseJson.of(ErrorCode.INVALID_OTP, e.getMessage());
        }
    }


    @PostMapping("/introspect")
    public String verifyToken(@RequestBody IntrospectRequest intro) throws ParseException {
        IntrospectResponse result = jwtService.introspect(intro);
        return ResponseJson.success("Token introspection successful", result);
    }

    @PostMapping("/refresh")
    public String refresh(@RequestBody RefreshRequest refreshRequest) {
        try {
            User user = userService.getUserFromRefreshToken(refreshRequest.getToken());
            if (user == null || user.getId().equals(String.valueOf(Config.CODE.ERROR_CODE_103))) {
                logger.warn("Invalid refresh token: {}", refreshRequest.getToken());
                return ResponseJson.of(ErrorCode.UNAUTHENTICATED, "Invalid refresh token");
            } else {
                // kiem tra xem access_token da het han hay chua
                User userVerifyAccessToken = jwtService.verifyAcessToken(user.getAccess_token());

                if ((userVerifyAccessToken != null && userVerifyAccessToken.getId().equals(String.valueOf(Config.CODE.ERROR_CODE_103)))) {
                    logger.info("Access token expired for userId: {}. Refreshing token.", user.getId());
                    user = userService.refreshToken(user.getId());
                    logger.info("Token refresh successful for userId: {}", userVerifyAccessToken.getId());
                    var result = AuthenticationResponse.builder()
                            .token(user.getAccess_token())
                            .build();

                    return ResponseJson.success("Token refresh successful", result);
                } else {
                    logger.warn("Refresh token has not expired yet for userId: {}", user.getId());
                    return ResponseJson.of(ErrorCode.UNAUTHENTICATED, "Refresh token has not expired yet");
                }

            }
        } catch (Exception e) {
            logger.error("Token refresh failed for token: {} - {}", refreshRequest.getToken(), e.getMessage(), e);
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, e.getMessage());
        }
    }


}
