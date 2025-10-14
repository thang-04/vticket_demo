package com.vticket.vticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MomoCreationResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String payUrl;
    private String deeplink;
    private String resultCode;
    private String message;
    private String payType;
    private String responseTime;
    private String qrCodeUrl;
}
