package com.vticket.vticket.domain.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ticket_types")
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long eventId;
    private String name;          // "Red Rose", "Blue Rose"
    private String color;
    private Double price;
    private Double original_price;
    private Integer max_qty_per_order;
    private Integer min_qty_per_order;
    private Integer quantity_sale; // tổng số vé có thể bán
    private Boolean is_discount;
    private Boolean is_free;
    private Integer discount_percent;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    public enum TicketStatus {
        AVAILABLE, SOLD_OUT, STOPPED
    }
}
