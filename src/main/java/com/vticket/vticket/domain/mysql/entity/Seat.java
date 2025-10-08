package com.vticket.vticket.domain.mysql.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seats")
public class Seat {
    @Id
    private Long id;
    private Long event_id;
    private String seat_row;
    private String seat_column;
    private String seat_type;
    private String zone;
    private SeatStatus status;
    private Double price;


    public enum SeatStatus {
        AVAILABLE,
        HOLD,
        SOLD
    }
}


