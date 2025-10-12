package com.vticket.vticket.domain.mysql.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_code", unique = true, nullable = false, length = 50)
    private String bookingCode;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "seat_ids", nullable = false, columnDefinition = "TEXT")
    private String seatIds; // "1,2,3" - comma separated seat IDs
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Column(name = "discount_code", length = 50)
    private String discountCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;
    
    @Column(name = "momo_order_id", length = 100)
    private String momoOrderId;
    
    @Column(name = "momo_transaction_id", length = 100)
    private String momoTransactionId;
    
    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;
    
    @Column(name = "payment_code", length = 100)
    private String paymentCode;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    public enum BookingStatus {
        PENDING,    // Đang chờ thanh toán
        PAID,       // Đã thanh toán thành công
        EXPIRED,    // Hết hạn chưa thanh toán
        CANCELLED   // Đã hủy
    }
    
    public enum PaymentMethod {
        MOMO, 
        VNPAY, 
        BANK_TRANSFER,
        CASH
    }
    

}
