package com.vticket.vticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketItemResponse {
    private Long id;
    private Long event_id;
    private String ticket_name;
    private String description = "";
    private String color;
    private Boolean is_free;
    private Double price;
    private Double original_price;
    private Boolean is_discount;
    private Integer discount_percent;
    private Double discount_total = 0.0;
    private Integer quantity;
    private List<SeatResponse> seats;
}
