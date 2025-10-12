package com.vticket.vticket.dto.response;

import com.vticket.vticket.dto.request.ListItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitTicketResponse {
    private Long eventId;
    private String bookingCode;
    private Long bookingId;
    private BigDecimal discount;
    private Long expiredIn;
    private List<ListItem> listItem;
    private BigDecimal subtotal;
    private BigDecimal totalAmount;
    private String paymentCode;
    private LocalDateTime expiredAt;
    private EventInfo eventInfo;
}
