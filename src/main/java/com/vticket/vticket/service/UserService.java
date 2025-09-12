package com.vticket.vticket.service;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.exception.AppException;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.mapper.UserMapper;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserCollection userCollection;
    @Autowired
    private UserMapper userMapper;
    //    @Autowired
//    PasswordEncoder passwordEncoder;
    @Autowired
    private ProcessUserService processUserService;
    @Autowired
    private JwtService jwtService;


    public UserResponse createUser(UserCreationRequest userCreationRequest) {
        try {
            if (userCollection.getUserInfoByUserName(userCreationRequest.getUsername()) != null) {
                throw new RuntimeException("UserName already in use");
            }
            User user = userMapper.toEntity(userCreationRequest);
            user.setPassword(userCreationRequest.getPassword());

            Date expireDate = new Date(System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000);
            Date expireDateRefresh = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

            String accessToken = jwtService.generateToken(user, expireDate);//15 ngày
            user.setAccess_token(accessToken);

            String refreshToken = jwtService.generateToken(user, expireDateRefresh);//30 ngày
            user.setRefresh_token(refreshToken);

//            processUserService.enQueueUser(user);
            userCollection.insertUser(user);
            //day vao redis
            return userMapper.toResponse(user);

        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
    }

    public List<UserResponse> getAllUser() {
        List<User> users = userCollection.getAllUsers();
        if (users.isEmpty()) {
            throw new RuntimeException("No users found");
        }
        return users.stream().map(userMapper::toResponse).toList();
    }

    public User getUserByUserName(String username) {
        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("Username is required");
        }
        User user = userCollection.getUserInfoByUserName(username);
        if (user == null) {
            throw new RuntimeException("User not found with username: " + username);
        }
        return user;
    }

    public User getUserById(String id) {
        if (StringUtils.isBlank(id)) {
            throw new RuntimeException("Id is required");
        }
        User user = userCollection.getUserById(id);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user;
    }

    public User getUserFromAccessToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("Access Token is required");
        }
        User user = null;
        try {
            user = jwtService.verifyAcessToken(token);
            if (user != null && Long.parseLong(user.getId()) > 0) {
                user = getUserById(user.getId());
                if (user != null && !token.equals(user.getAccess_token())) {
                    user = null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user with token: " + token);
        }
        return user;
    }

    public User getUserFromRefreshToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("Refresh Token is required");
        }

        User user = null;
        try {
            user = jwtService.verifyRefreshToken(token);
            if (user != null && user.getId() != null && Long.parseLong(user.getId()) > 0) {
                user = getUserById(user.getId());
                if (user != null && !token.equals(user.getRefresh_token())) {
                    user = null;
                }
            }
        } catch (Exception ex) {
//            logger.error(session + "|getUserFromRefreshToken|token=" + token + "|Exception=" + ex.getMessage(), ex);
        }
        return user;
    }

    public User refreshToken(String user_id) {
        User user = this.getUserById(user_id);
        try {
            if (user == null) {
                return null;
            }
            String oldAccessToken = user.getAccess_token();
            user.setAccess_token("");

            Date expireDate = new Date(System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000);
            Date expireDateRefresh = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

            String accessToken = jwtService.generateToken(user, expireDate);//15 ngày
            user.setAccess_token(accessToken);

            String refreshToken = jwtService.generateToken(user, expireDateRefresh);//30 ngày
            user.setRefresh_token(refreshToken);

            if (!userCollection.updateTokenOfUser(user, expireDate)) {
                user = null;
            }//else update redis

        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
