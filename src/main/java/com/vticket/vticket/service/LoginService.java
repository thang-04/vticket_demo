package com.vticket.vticket.service;

import ch.qos.logback.core.util.StringUtil;
import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.AuthenticationRequest;
import com.vticket.vticket.dto.response.AuthenticationResponse;
import com.vticket.vticket.exception.AppException;
import com.vticket.vticket.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserService userService;

    public AuthenticationResponse authentication(AuthenticationRequest request) {
//        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        User user = userService.getUserByUserName(request.getUsername());
        boolean authenticated = user.getPassword().equals(request.getPassword());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (StringUtil.isNullOrEmpty(user.getAccess_token())) {
            user = userService.refreshToken(user.getId());
        } else {
            User user2 = userService.getUserFromAccessToken(user.getAccess_token());
            if (user2 != null && Long.parseLong(user2.getId()) == Config.CODE.ERROR_CODE_103) {
                user = userService.refreshToken(user.getId());
            }
        }
        return AuthenticationResponse.builder()
                .token(user.getAccess_token())
                .message("Success")
                .build();

    }

}
