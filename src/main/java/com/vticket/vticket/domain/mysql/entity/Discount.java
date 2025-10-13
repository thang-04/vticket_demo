//package com.vticket.vticket.domain.mysql.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@Entity
//@Table(name = "discounts")
//public class Discount {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "code", unique = true, nullable = false, length = 50)
//    private String code;
//
//    @Column(name = "name", nullable = false, length = 255)
//    private String name;
//
//    @Column(name = "description", columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "value", nullable = false)
//    private Double value;
//
//    @Column(name = "max_amount")
//    private Double maxAmount;
//
//    @Column(name = "max_usage", nullable = false)
//    private Integer maxUsage;
//
//    @Column(name = "used_count", nullable = false)
//    private Integer usedCount;
//
//    @Column(name = "start_date", nullable = false)
//    private LocalDateTime startDate;
//
//    @Column(name = "end_date", nullable = false)
//    private LocalDateTime endDate;
//
//    @Column(name = "is_active", nullable = false)
//    private Boolean isActive;
//
//    @Column(name = "event_id")
//    private Long eventId;
//
//    @Column(name = "min_order_amount")
//    private Double minOrderAmount;
//
//    @Column(name = "max_discount_per_user")
//    private Double maxDiscountPerUser;
//
//    @Column(name = "is_one_time_use", nullable = false)
//    private Boolean isOneTimeUse;
//
//    @CreationTimestamp
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    @Column(name = "updated_at", nullable = false)
//    private LocalDateTime updatedAt;
//
//    @Column(name = "created_by")
//    private Long createdBy;
//
//    @Column(name = "updated_by")
//    private Long updatedBy;
//
//}
