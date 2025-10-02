package com.vticket.vticket.service;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.AuthenticationRequest;
import com.vticket.vticket.dto.response.AuthenticationResponse;
import com.vticket.vticket.exception.AppException;
import com.vticket.vticket.exception.ErrorCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LoginService {

    private static final Logger logger = LogManager.getLogger(LoginService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    public AuthenticationResponse authentication(AuthenticationRequest request) {
        long start = System.currentTimeMillis();
        logger.info("Authenticating user: {}", request.getUsername());

        User user = userService.getUserByUserName(request.getUsername());
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) {
            logger.warn("Authentication failed for user: {}", request.getUsername());
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        // Check if the user already has a valid token
        if (user.getAccess_token() != null && !user.getAccess_token().isEmpty()) {
            User tokenValidationResult = userService.getUserFromAccessToken(user.getAccess_token());

            if (tokenValidationResult != null && !Objects.equals(tokenValidationResult.getId(), String.valueOf(Config.CODE.ERROR_CODE_103))) {
                logger.info("Token is still valid for user: {}", request.getUsername());
                return AuthenticationResponse.builder()
                        .access_token(user.getAccess_token())
                        .refresh_token(user.getRefresh_token())
                        .build();
            } else {
                // Token is expired or invalid
                logger.info("Token expired for user: {}, refreshing token", request.getUsername());
                user = userService.refreshToken(user.getId());
            }
        } else {
            logger.info("No existing token for user: {}, creating new token", request.getUsername());

            // First time login, send welcome email
            emailService.sendWelcomeEmail(user);

            user = userService.refreshToken(user.getId());
        }

        return AuthenticationResponse.builder()
                .access_token(user.getAccess_token())
                .refresh_token(user.getRefresh_token())
                .build();
    }


}
