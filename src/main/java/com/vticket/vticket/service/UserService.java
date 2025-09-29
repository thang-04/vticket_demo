package com.vticket.vticket.service;

import com.google.gson.reflect.TypeToken;
import com.vticket.vticket.config.RedisKey;
import com.vticket.vticket.domain.mongodb.entity.Role;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mongodb.repo.RoleCollection;
import com.vticket.vticket.domain.mongodb.repo.UserCollection;
import com.vticket.vticket.dto.request.ChangePasswordRequest;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.request.UserUpdateRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.mapper.UserMapper;
import com.vticket.vticket.utils.PredefinedRole;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vticket.vticket.utils.CommonUtils.gson;

@Service
public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserCollection userCollection;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    private ProcessUserService processUserService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private RoleCollection roleCollection;
    @Autowired
    private FileUploadService fileUploadService;


    public UserResponse createNewUser(UserCreationRequest userCreationRequest) {
        logger.info("Creating new user with username: {}", userCreationRequest.getUsername());
        long start = System.currentTimeMillis();

        try {
            if (userCollection.getUserInfoByUserName(userCreationRequest.getUsername()) != null) {
                logger.warn("Username already exists: {}", userCreationRequest.getUsername());
            } else {
                User user = userMapper.toEntity(userCreationRequest);
                user.setPassword(passwordEncoder.encode(userCreationRequest.getPassword()));

                Date expireDate = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
                Date expireDateRefresh = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

                String accessToken = jwtService.generateToken(user, expireDate);//15 ngày
                user.setAccess_token(accessToken);

                String refreshToken = jwtService.generateToken(user, expireDateRefresh);//30 ngày
                user.setRefresh_token(refreshToken);

                HashSet<Role> roles = new HashSet<>();
                if (roleCollection.getRoleByName(PredefinedRole.USER_ROLE) != null) {
                    roles.add(roleCollection.getRoleByName(PredefinedRole.USER_ROLE));
                } else {
                    logger.error("Default role not found: {}", PredefinedRole.USER_ROLE);
                }

                user.setRoles(roles);

//            processUserService.enQueueUser(user);

                // send otp
                if (registrationService.sendRegistrationOtp(user)) {
                    // add user to mongo
                    User savedUser = userCollection.insertUser(user);
                    logger.info("Successfully created user: {} with ID: {} and time: {}", userCreationRequest.getUsername(), savedUser.getId(), (System.currentTimeMillis() - start));
                    // add user to redis
                    putUserInfoToRedis(savedUser);

                    return userMapper.toResponse(savedUser);
                }
            }
        } catch (Exception e) {
            logger.error("Error creating user: {} - {}", userCreationRequest.getUsername(), e.getMessage(), e);
        }
        logger.info("Failed to create user: {} with time: {}", userCreationRequest.getUsername(), (System.currentTimeMillis() - start));
        return null;
    }

    public void putUserInfoToRedis(User user) {
        String keyRedis = RedisKey.USER_ID + user.getId();
        //add user to redis
        redisService.getRedisSsoUser().opsForValue().set(keyRedis, gson.toJson(user));
        //set time expire 30p
        redisService.getRedisSsoUser().expire(keyRedis, 30L, TimeUnit.MINUTES);
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
        long start = System.currentTimeMillis();
        String dataFrom = "BY REDIS";
        String keyRedis = RedisKey.USER_ID + id;
        String resultRedis;
        User user = null;
        try {
            // check in redis
            resultRedis = redisService.getRedisSsoUser().opsForValue().get(keyRedis);
            if (!StringUtils.isBlank(resultRedis)) {
                user = gson.fromJson(resultRedis, new TypeToken<User>() {
                }.getType());
                logger.info("User found in Redis cache  : {} with time: {}ms", user, (System.currentTimeMillis() - start));
            } else {
                // get in mongo
                user = userCollection.getUserById(id);
                dataFrom = "BY MONGO";
                logger.info("User retrieved from MONGO  : {} with time: {}ms", user, (System.currentTimeMillis() - start));
                if (user == null) {
                    logger.info("User not found with ID: {}", id);
                } else {
                    putUserInfoToRedis(user);
                }
            }
        } catch (Exception e) {
            logger.error("Error accessing Redis for user ID: {} - {}", id, e.getMessage(), e);
        }
        logger.info("Successfully retrieved " + dataFrom + " user: " + id);
        return user;
    }

    public UserResponse getUserByUId(String id) {
        logger.debug("Retrieving user by ID: {}", id);
        long start = System.currentTimeMillis();
        String dataFrom = "BY REDIS";
        String keyRedis = RedisKey.USER_ID + id;
        String resultRedis;
        User user = null;
        try {
            // check in redis
            resultRedis = redisService.getRedisSsoUser().opsForValue().get(keyRedis);
            if (!StringUtils.isBlank(resultRedis)) {
                user = gson.fromJson(resultRedis, new TypeToken<User>() {
                }.getType());
                logger.info("User found in Redis cache for : {} with time: {}", user, (System.currentTimeMillis() - start));
            } else {
                // get in mongo
                user = userCollection.getUserById(id);
                dataFrom = "BY MONGO";
                logger.info("User retrieved from MONGO for : {} with time: {}", user, (System.currentTimeMillis() - start));
                if (user == null) {
                    logger.info("User not found with ID: {}", id);
                } else {
                    putUserInfoToRedis(user);
                }
            }
        } catch (Exception e) {
            logger.error("Error accessing Redis for user ID: {} - {}", id, e.getMessage(), e);
        }
        logger.info("Successfully retrieved " + dataFrom + " user: " + id);
        return userMapper.toResponse(user);
    }

    public User getUserFromAccessToken(String token) {
        logger.debug("Retrieving user from access token");
        if (StringUtils.isBlank(token)) {
            logger.warn("Access token is blank or null");
        }
        User user = null;
        try {
            user = jwtService.verifyAcessToken(token);
            if (user != null && user.getId() != null) {
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
        }

        User user = null;
        try {
            user = jwtService.verifyRefreshToken(token);
            if (user != null && user.getId() != null) {
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

            String oldAccessToken = user.getAccess_token();
            String oldRefreshToken = user.getRefresh_token();

            logger.debug("Old tokens for user {} - Access: {}, Refresh: {}", user_id,
                    oldAccessToken != null ? "***" + oldAccessToken.substring(Math.max(0, oldAccessToken.length() - 4)) : "null",
                    oldRefreshToken != null ? "***" + oldRefreshToken.substring(Math.max(0, oldRefreshToken.length() - 4)) : "null");

            // Invalidate old_token
            user.setAccess_token("");
            user.setRefresh_token("");

            // Create new token
            Date expireDate = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
            Date expireDateRefresh = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);

            String accessToken = jwtService.generateToken(user, expireDate);//15 ngày
            user.setAccess_token(accessToken);

            String refreshToken = jwtService.generateToken(user, expireDateRefresh);//30 ngày
            user.setRefresh_token(refreshToken);

            // update mongoDB
            if (userCollection.updateTokenOfUser(user, expireDate)) {
                redisService.deleteRedisUser(user);
                logger.info("Successfully refreshed tokens for user: {}", user_id);

            } else {
                logger.error("Failed to update token in database for user: {}", user_id);
                user = null;
            }

        } catch (Exception e) {
            logger.error("Error refreshing token for user: {} - {}", user_id, e.getMessage(), e);
            return null;
        }
        return user;
    }

    public UserResponse getMyInfo() {
        long start = System.currentTimeMillis();
        var context = SecurityContextHolder.getContext();
        // username from context is sub in jwt token
        String username = context.getAuthentication().getName();
        logger.info("Get info authentication for username: {}", username);
        User user = userCollection.getUserInfoByUserName(username);
        logger.info("Retrieved user info for {}: {} with time: {}ms", username, user, (System.currentTimeMillis() - start));
        if (user == null) {
            logger.info("User not found in context: {}", username);
            return null;
        }
        return userMapper.toResponse(user);
    }

    public boolean updateUserProfile(String userId, UserUpdateRequest req, MultipartFile avatar) throws IOException {
        long start = System.currentTimeMillis();
        logger.info("Updating user profile for user ID: {} with data: {}", userId, gson.toJson(req));
        String filePathImg = null;

        if (avatar != null && !avatar.isEmpty()) {
            logger.info("New avatar file detected for user {}. Uploading...", userId);
            filePathImg = fileUploadService.uploadFileImg(avatar);

            if (filePathImg == null) {
                logger.error("Avatar upload failed for user {}. Aborting profile update.", userId);
                return false;
            }
        } else {
            logger.info("No new avatar file provided for user {}. Skipping image upload.", userId);
        }
        return userCollection.updateUserInfo(userId, req, filePathImg);
    }

    public boolean deleteUserAccount(User user) {
        boolean result = false;
        long start = System.currentTimeMillis();
        String key = "";
        try {
            logger.info("|deleteUserAccount| START User =" + user.getId());

            String userName = user.getUsername();
            if (StringUtils.isNotEmpty(userName)) {
                key = RedisKey.USER_TYPE_LOGIN + ":" + user.getUsername();
                userName = userName + "_del_" + start;
                redisService.getRedisSsoUser().delete(key);
            }

            String email = user.getEmail();
            if (StringUtils.isNotEmpty(email)) {
                key = RedisKey.USER_TYPE_LOGIN + ":" + email;
                email = email + "_del_" + start;
                redisService.getRedisSsoUser().delete(key);
            }

            boolean resultUpdate = userCollection.updateUserDeleteAccount(user.getId(), userName, email);
            if (resultUpdate) {
                result = true;
                redisService.getRedisSsoUser().delete(key);
            }
        } catch (Exception e) {
            logger.error("|deleteUserAccount|" + e.getMessage(), e);
        }
        logger.info("|deleteUserAccount|" + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    public boolean changePassword(ChangePasswordRequest request, String accessToken) {
        long start = System.currentTimeMillis();
        User user = getUserFromAccessToken(accessToken);
        if (user == null) {
            logger.info("User not found in accessToken: {}", accessToken);
            return false;
        }
        boolean isMatches = passwordEncoder.matches(request.getOldPassword(), user.getPassword());
        if (isMatches) {
            logger.info("Change password for user: {} success with time {}ms", user.getUsername(), (System.currentTimeMillis() - start));
            String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
            userCollection.updatePassword(user.getId(), encodedNewPassword);
            return true;
        } else {
            logger.info("Old password does not match for user: {}", user.getUsername());
            return false;
        }
    }

}
