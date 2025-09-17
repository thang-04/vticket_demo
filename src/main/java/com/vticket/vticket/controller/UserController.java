package com.vticket.vticket.controller;


import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.UserService;
import com.vticket.vticket.service.RegistrationService;
import com.vticket.vticket.utils.ResponseJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private RegistrationService registrationService;

    @PostMapping()
    public String createUser(@RequestBody UserCreationRequest user) {
        try {
            UserResponse userrespone = userService.createUser(user);
            return ResponseJson.success("Create user successful", userrespone);
        } catch (Exception e) {
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
        try {
            UserResponse userrespone = userService.getUserByUId(id);
            return ResponseJson.success("Get user by id successful", userrespone);
        } catch (Exception e) {
            return ResponseJson.of(ErrorCode.USER_NOT_EXISTED, e.getMessage());
        }
    }

    @PostMapping("/register")
    public String register(@RequestBody UserCreationRequest user) {
        try {
            registrationService.sendRegistrationOtp(user);
            return ResponseJson.success("OTP sent to email", null);
        } catch (Exception e) {
            return ResponseJson.of(ErrorCode.UNCATEGORIZED_EXCEPTION, e.getMessage());
        }
    }
}
