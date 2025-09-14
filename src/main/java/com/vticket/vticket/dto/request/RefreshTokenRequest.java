package com.vticket.vticket.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    private String refreshToken;
}
