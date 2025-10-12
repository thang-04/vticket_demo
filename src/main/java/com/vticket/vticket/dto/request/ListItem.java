package com.vticket.vticket.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListItem {
    private Long seatId;
    private String seatName;
    private String rowName;
    private Integer seatNumber;
    private Integer columnNumber;
    private BigDecimal price;
    private Long ticketTypeId;
    private String ticketTypeName;
    private String ticketTypeColor;
    private Integer quantity;
    private BigDecimal itemTotal;
}
