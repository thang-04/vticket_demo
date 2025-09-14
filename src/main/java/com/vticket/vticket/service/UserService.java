package com.vticket.vticket.service;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.exception.AppException;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.mapper.UserMapper;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

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
        logger.info("Creating new user with username: {}", userCreationRequest.getUsername());
        try {
            if (userCollection.getUserInfoByUserName(userCreationRequest.getUsername()) != null) {
                logger.warn("Username already exists: {}", userCreationRequest.getUsername());
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
            logger.info("Successfully created user: {}", userCreationRequest.getUsername());
            //day vao redis
            return userMapper.toResponse(user);

        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating user: {} - {}", userCreationRequest.getUsername(), e.getMessage());
            throw new AppException(ErrorCode.USER_EXISTED);
        } catch (Exception e) {
            logger.error("Error creating user: {} - {}", userCreationRequest.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    public List<UserResponse> getAllUser() {
        logger.debug("Retrieving all users");
        List<User> users = userCollection.getAllUsers();
        if (users.isEmpty()) {
            logger.warn("No users found in database");
            throw new RuntimeException("No users found");
        }
        logger.info("Successfully retrieved {} users", users.size());
        return users.stream().map(userMapper::toResponse).toList();
    }

    public User getUserByUserName(String username) {
        logger.debug("Retrieving user by username: {}", username);
        if (StringUtils.isBlank(username)) {
            logger.warn("Username is blank or null");
            throw new RuntimeException("Username is required");
        }
        User user = userCollection.getUserInfoByUserName(username);
        if (user == null) {
            logger.warn("User not found with username: {}", username);
            throw new RuntimeException("User not found with username: " + username);
        }
        logger.debug("Successfully retrieved user: {}", username);
        return user;
    }

    public User getUserById(String id) {
        logger.debug("Retrieving user by ID: {}", id);
        if (StringUtils.isBlank(id)) {
            logger.warn("User ID is blank or null");
            throw new RuntimeException("Id is required");
        }
        User user = userCollection.getUserById(id);
        if (user == null) {
            logger.warn("User not found with ID: {}", id);
            throw new RuntimeException("User not found with id: " + id);
        }
        logger.debug("Successfully retrieved user by ID: {}", id);
        return user;
    }

    public User getUserFromAccessToken(String token) {
        logger.debug("Retrieving user from access token");
        if (StringUtils.isBlank(token)) {
            logger.warn("Access token is blank or null");
            throw new RuntimeException("Access Token is required");
        }
        User user = null;
        try {
            user = jwtService.verifyAcessToken(token);
            if (user != null && Long.parseLong(user.getId()) > 0) {
                user = getUserById(user.getId());
                if (user != null && !token.equals(user.getAccess_token())) {
                    logger.warn("Token mismatch for user ID: {}", user.getId());
                    user = null;
                } else if (user != null) {
                    logger.debug("Successfully retrieved user from access token for user ID: {}", user.getId());
                }
            }
        } catch (Exception e) {
            logger.error("Error retrieving user with access token: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving user with token: " + token);
        }
        return user;
    }

    public User getUserFromRefreshToken(String token) {
        logger.debug("Retrieving user from refresh token");
        if (StringUtils.isBlank(token)) {
            logger.warn("Refresh token is blank or null");
            throw new RuntimeException("Refresh Token is required");
        }

        User user = null;
        try {
            user = jwtService.verifyRefreshToken(token);
            if (user != null && user.getId() != null && Long.parseLong(user.getId()) > 0) {
                user = getUserById(user.getId());
                if (user != null && !token.equals(user.getRefresh_token())) {
                    logger.warn("Refresh token mismatch for user ID: {}", user.getId());
                    user = null;
                } else if (user != null) {
                    logger.debug("Successfully retrieved user from refresh token for user ID: {}", user.getId());
                }
            }
        } catch (Exception ex) {
            logger.error("Error retrieving user with refresh token: {}", ex.getMessage(), ex);
        }
        return user;
    }

    public User refreshToken(String user_id) {
        logger.info("Refreshing token for user ID: {}", user_id);
        User user = this.getUserById(user_id);
        try {
            if (user == null) {
                logger.warn("User not found for token refresh: {}", user_id);
                return null;
            }
            
            // Lưu token cũ để log
            String oldAccessToken = user.getAccess_token();
            String oldRefreshToken = user.getRefresh_token();
            
            logger.debug("Old tokens for user {} - Access: {}, Refresh: {}", user_id, 
                oldAccessToken != null ? "***" + oldAccessToken.substring(Math.max(0, oldAccessToken.length() - 4)) : "null",
                oldRefreshToken != null ? "***" + oldRefreshToken.substring(Math.max(0, oldRefreshToken.length() - 4)) : "null");
            
            // Invalidate token cũ ngay lập tức
            user.setAccess_token("");
            user.setRefresh_token("");

            // Tạo token mới
            Date expireDate = new Date(System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000);
            Date expireDateRefresh = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

            String accessToken = jwtService.generateToken(user, expireDate);//15 ngày
            user.setAccess_token(accessToken);

            String refreshToken = jwtService.generateToken(user, expireDateRefresh);//30 ngày
            user.setRefresh_token(refreshToken);

            // Cập nhật database
            if (!userCollection.updateTokenOfUser(user, expireDate)) {
                logger.error("Failed to update token in database for user: {}", user_id);
                user = null;
            } else {
                logger.info("Successfully refreshed tokens for user: {}", user_id);
                // TODO: Implement token blacklist service if needed
                // tokenBlacklistService.addToBlacklist(oldAccessToken);
                // tokenBlacklistService.addToBlacklist(oldRefreshToken);
            }

        } catch (Exception e) {
            logger.error("Error refreshing token for user: {} - {}", user_id, e.getMessage(), e);
            return null;
        }
        return user;
    }
}
