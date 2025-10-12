package com.vticket.vticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitTicketRequest {
    private Long eventId;
    private List<ListItem> listItem;
    private Long timestamp;
    private String discountCode;
    private String paymentMethod;
}
