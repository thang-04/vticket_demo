package com.vticket.vticket.dto.response;

import com.vticket.vticket.domain.mysql.entity.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusResponse {
    private Booking.BookingStatus status;
    private String paymentUrl;
    private LocalDateTime expiredAt;
}
