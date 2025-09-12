package com.vticket.vticket.dto.response;

import com.vticket.vticket.domain.mongodb.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String fullName;
    private String username;
    private String email;
    private String address;
    private String avatar;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private Set<Role> roles;
    private boolean isActive;
}
