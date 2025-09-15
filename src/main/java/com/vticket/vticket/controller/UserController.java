package com.vticket.vticket.controller;


import com.vticket.vticket.dto.response.ApiResponse;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.response.UserResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping()
    public ApiResponse<UserResponse> createUser(@RequestBody UserCreationRequest user) {
        try {
            UserResponse userrespone = userService.createUser(user);
            return ApiResponse.<UserResponse>builder()
                    .result(userrespone)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<UserResponse>builder()
                    .code(ErrorCode.USER_EXISTED.getCode())
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }

//    @GetMapping
//    public ApiResponse<List<UserResponse>> getAllUsers() {
//        return ApiResponse.<List<UserResponse>>builder()
//                .result(userService.getAllUser())
//                .build();
//    }

    @GetMapping()
    public ApiResponse<UserResponse> getUserById() {
        try {
            UserResponse userrespone = userService.getMyInfo();
            return ApiResponse.<UserResponse>builder()
                    .result(userrespone)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<UserResponse>builder()
                    .code(ErrorCode.USER_EXISTED.getCode())
                    .message(e.getMessage())
                    .result(null)
                    .build();
        }
    }
}
