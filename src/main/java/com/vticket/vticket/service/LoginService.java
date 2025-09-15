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
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LogManager.getLogger(LoginService.class);

    @Autowired
    private UserService userService;

    public AuthenticationResponse authentication(AuthenticationRequest request) {
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        logger.info("Authenticating user: {}", request.getUsername());
        User user = userService.getUserByUserName(request.getUsername());
        boolean authenticated = user.getPassword().equals(request.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);
        logger.warn("Authentication failed for user: {}", request.getUsername());

     if (user.getAccess_token() != null && !user.getAccess_token().isEmpty()) {
         logger.info("Refreshing token for user: {}", user.getUsername());
         user = userService.refreshToken(user.getId());
        } else {
            User user2 = userService.getUserFromAccessToken(user.getAccess_token());
            if (user2 != null && Long.parseLong(user2.getId()) == Config.CODE.ERROR_CODE_103) {
                user = userService.refreshToken(user.getId());
            }
        }
        return AuthenticationResponse.builder()
                .token(user.getAccess_token())
                .build();

    }

}
