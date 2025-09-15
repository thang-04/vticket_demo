package com.vticket.vticket.controller;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.AuthenticationRequest;
import com.vticket.vticket.dto.request.IntrospectRequest;
import com.vticket.vticket.dto.request.RefreshRequest;
import com.vticket.vticket.dto.response.ApiResponse;
import com.vticket.vticket.dto.response.AuthenticationResponse;
import com.vticket.vticket.dto.response.IntrospectResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.JwtService;
import com.vticket.vticket.service.LoginService;
import com.vticket.vticket.service.UserService;
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
                    .code(ErrorCode.UNAUTHENTICATED.getCode())
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }

@PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> verifyToken(@RequestBody IntrospectRequest intro) throws ParseException {
    IntrospectResponse result = jwtService.introspect(intro);
       return ApiResponse.<IntrospectResponse>builder()
               .message("Token introspection successful")
               .result(result)
               .build();
}

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        try {
            User user = userService.getUserFromRefreshToken(refreshRequest.getToken());
            if (user == null || user.getId().equals(String.valueOf(Config.CODE.ERROR_CODE_103))) {
                logger.warn("Invalid refresh token: {}", refreshRequest.getToken());
                return ApiResponse.<AuthenticationResponse>builder()
                        .code(ErrorCode.UNAUTHENTICATED.getCode())
                        .message("Invalid refresh token")
                        .result(null)
                        .build();
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

                    return ApiResponse.<AuthenticationResponse>builder()
                            .message("Token refresh successful")
                            .result(result)
                            .build();
                }else{
                    logger.warn("Refresh token has not expired yet for userId: {}", user.getId());
                    return ApiResponse.<AuthenticationResponse>builder()
                            .code(ErrorCode.UNAUTHENTICATED.getCode())
                            .message("Refresh token has not expired yet")
                            .result(null)
                            .build();
                }

            }
        } catch (Exception e) {
            logger.error("Token refresh failed for token: {} - {}", refreshRequest.getToken(), e.getMessage(), e);
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(ErrorCode.UNAUTHENTICATED.getCode())
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }


}
