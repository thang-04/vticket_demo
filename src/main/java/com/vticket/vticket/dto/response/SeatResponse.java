package com.vticket.vticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {
    private Long id;
    private Long ticket_type_id;
    private String seat_name;
    private Integer seat_number;
    private String row_name;
    private Integer column_number;
    private Integer quantity;
    private Double discount_total;
}

