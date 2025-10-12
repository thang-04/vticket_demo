package com.vticket.vticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MomoPaymentResponse {
    private boolean success;
    private String orderId;
    private String payUrl;
    private String message;
}
