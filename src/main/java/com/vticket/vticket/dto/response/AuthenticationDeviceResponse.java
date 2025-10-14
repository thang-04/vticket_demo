package com.vticket.vticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationDeviceResponse {
    private String access_token;
    private String verify_token;
    private String device_id;
    private Long expired_at;
}
