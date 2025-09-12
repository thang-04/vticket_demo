package com.vticket.vticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String address;
}
