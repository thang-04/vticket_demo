package com.vticket.vticket.domain.mysql.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long event_id;
    private String title;
    private String description;
    private Double price;
    private String venue;
    private Date start_time;
    private Date end_time;
    private Date created_at;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category_id;
}
