package com.vticket.vticket.domain.mysql.entity;

import jakarta.persistence.*;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private String seat_name;      // A-1, A-2
    private String row_name;       // A, B, C
    private Integer seat_number;
    private Integer column_number;

    @Enumerated(EnumType.ORDINAL)
    private SeatStatus status;
    private Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType;

    public enum SeatStatus {
        AVAILABLE, HOLD, SOLD
    }
}


