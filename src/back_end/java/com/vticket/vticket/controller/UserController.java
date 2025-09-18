package com.vticket.vticket.controller;


import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.OtpVerifyRequest;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.request.UserUpdateRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.RedisService;
import com.vticket.vticket.service.RegistrationService;
import com.vticket.vticket.service.UserService;
import com.vticket.vticket.utils.ResponseJson;
import jakarta.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.vticket.vticket.utils.CommonUtils.gson;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final Logger logger = Logger.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RegistrationService registrationService;


    @PostMapping()
    public String createUser(@RequestBody @Valid UserCreationRequest user, BindingResult bindingResult) {
        logger.info("Received request to create user: " + user);
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid request");
            logger.info("User creation failed due to validation errors: " + errorMessage);
            return ResponseJson.of(ErrorCode.INVALID_REGISTER, errorMessage);
        }
        try {
            UserResponse userrespone = userService.createNewUser(user);
            if (userrespone == null) {
                return ResponseJson.of(ErrorCode.USER_EXISTED, "User existed");
            }
            logger.info("User created successfully: " + userrespone);
            return ResponseJson.success("Create user successful", userrespone);
        } catch (Exception e) {
            logger.error("Exception during user creation: ", e);
            return ResponseJson.of(ErrorCode.USER_EXISTED, e.getMessage());
        }
    }

//    @GetMapping
//    public ApiResponse<List<UserResponse>> getAllUsers() {
//        return ApiResponse.<List<UserResponse>>builder()
//                .result(userService.getAllUser())
//                .build();
//    }

    @GetMapping()
    public String getInfoUser() {
        try {
            UserResponse userrespone = userService.getMyInfo();
            return ResponseJson.success("Get user info successful", userrespone);
        } catch (Exception e) {
            return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public String getUserById(@PathVariable String id) {
        long startTime = System.currentTimeMillis();
        logger.info("Received request to get user by id:  " + id);
        try {
            UserResponse userrespone = userService.getUserByUId(id);
            return ResponseJson.success("Get user by id successful", userrespone);
        } catch (Exception e) {
            logger.error("Exception while fetching user by id: ", e);
            return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, e.getMessage());
        }
    }

    @PostMapping("/update")
    public String updateUser(@RequestBody @Valid UserUpdateRequest user,
                             BindingResult bindingResult,
                             @RequestHeader(value = "Authorization", required = false) String accessToken) {
        logger.info("Received request to update user: " + gson.toJson(user) + " with token: " + accessToken);
        if (accessToken != null) {
            accessToken = accessToken.replaceFirst("^Bearer\\s+", "");
        }
        long startTime = System.currentTimeMillis();
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(err -> err.getDefaultMessage())
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Invalid request");
            logger.info("User update failed due to validation errors: " + errorMessage);
            return ResponseJson.of(ErrorCode.INVALID_REGISTER, errorMessage);
        }
        try {
            //Get user from token
            User currentUser = userService.getUserFromAccessToken(accessToken);
            if (currentUser == null) {
                logger.info("User update failed: User not found for token " + accessToken +
                        ". Time taken: " + (System.currentTimeMillis() - startTime) + "ms");

                return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, "User not found");
            } else if (currentUser.getId().equals(String.valueOf(Config.CODE.ERROR_CODE_103))) {
                logger.info("User update failed: Token expired for token " + accessToken +
                        ". Time taken: " + (System.currentTimeMillis() - startTime) + "ms");

                return ResponseJson.of(ErrorCode.EXPIRED_TOKEN, "Token expired");
            }

            if (userService.updateUserProfile(currentUser.getId(), user)) {
                redisService.deleteRedisUser(currentUser);
                currentUser = userService.getUserById(currentUser.getId());
                logger.info("User updated successfully: " + currentUser +
                        ". Time taken: " + (System.currentTimeMillis() - startTime) + "ms");
                return ResponseJson.success("Update user successful", currentUser);
            } else {
                logger.info("User update failed for user: " + currentUser +
                        ". Time taken: " + (System.currentTimeMillis() - startTime) + "ms");
                return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, "User not found");
            }

        } catch (Exception e) {
            logger.error("Exception during user update: ", e);
            return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, e.getMessage());
        }
    }

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


    @PostMapping("/delete_account")
    public String deleteAccount(@RequestHeader(value = "Authorization", required = false) String accessToken) {
        logger.info("Received request to delete account with token: " + accessToken);
        if (accessToken != null) {
            accessToken = accessToken.replaceFirst("^Bearer\\s+", "");
        }
        try {
            User user = userService.getUserFromAccessToken(accessToken);
            if (user != null) {
                userService.deleteUserAccount(user);
                return ResponseJson.success("Delete account successful", null);
            } else {
                return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, "User not found");
            }
        } catch (Exception e) {
            return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, e.getMessage());
        }
    }
}
