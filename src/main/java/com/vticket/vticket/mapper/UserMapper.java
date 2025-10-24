package com.vticket.vticket.mapper;


import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.response.UserResponse;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class UserMapper {

    public User toEntity(UserCreationRequest request) {
        return User.builder()
                .full_name(request.getFirstName() + " " + request.getLastName())
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .address(request.getAddress())
                .created_at(new Date())
                .isActive(true)
                .avatar(null)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFull_name())
                .username(user.getUsername())
                .email(user.getEmail())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .createdAt(user.getCreated_at())
                .updatedAt(user.getUpdated_at())
                .roles(user.getRoles())
                .isActive(user.isActive())
                .build();
    }

}
