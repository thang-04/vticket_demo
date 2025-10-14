package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.domain.mysql.entity.Booking;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class BookingRepo {
    private static final Logger logger = Logger.getLogger(BookingRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;


    public List<Booking> getAllBookings() {
        try {
            String sql = "SELECT * FROM bookings ORDER BY created_at DESC";
            SqlParameterSource params = new MapSqlParameterSource();

            return jdbcTemplate.query(sql, params, bookingRowMapper());
        } catch (Exception ex) {
            logger.error("getAllBookings|Exception|{}" + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    public Booking getBookingById(Long id) {
        String logPrefix = "getBookingById|id=" + id;
        try {
            String sql = "SELECT * FROM bookings WHERE id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            return jdbcTemplate.queryForObject(sql, params, bookingRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.warn(logPrefix + "|No booking found");
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return null;
    }

    public List<Booking> getBookingsByUser(Long userId) {
        String logPrefix = "getBookingsByUser|userId=" + userId;
        try {
            String sql = "SELECT * FROM bookings WHERE user_id = :userId ORDER BY created_at DESC";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("userId", userId);

            return jdbcTemplate.query(sql, params, bookingRowMapper());
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    public boolean updateBookingStatus(Long id, Booking.BookingStatus status) {
        String logPrefix = "updateBookingStatus|id=" + id;
        try {
            String sql = "UPDATE bookings SET status = :status, updated_at = NOW() WHERE id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);
            params.addValue("status", status.name());

            int rows = jdbcTemplate.update(sql, params);
            return rows > 0;
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return false;
    }

    public Booking getBookingByBookingCode(String bookingCode) {
        String logPrefix = "getBookingByBookingCode|bookingCode=" + bookingCode;
        try {
            String sql = "SELECT * FROM bookings WHERE booking_code = :bookingCode";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("bookingCode", bookingCode);

            return jdbcTemplate.queryForObject(sql, params, bookingRowMapper());
        } catch (EmptyResultDataAccessException ex) {
            logger.warn(logPrefix + "|No booking found");
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return null;
    }

    public Long createBooking(Booking booking) {
        String logPrefix = "createBooking|bookingCode=" + booking.getBookingCode();
        try {
            String sql = """
                    INSERT INTO bookings (
                        booking_code, user_id, event_id, seat_ids,
                        total_amount, subtotal, discount_amount, discount_code,
                        status, payment_method, momo_order_id, momo_transaction_id,
                        payment_url, payment_code, expired_at, created_at, updated_at
                    ) VALUES (
                        :bookingCode, :userId, :eventId, :seatIds,
                        :totalAmount, :subtotal, :discountAmount, :discountCode,
                        :status, :paymentMethod, :momoOrderId, :momoTransactionId,
                        :paymentUrl, :paymentCode, :expiredAt, NOW(), NOW()
                    )
                    """;

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("bookingCode", booking.getBookingCode());
            params.addValue("userId", booking.getUserId());
            params.addValue("eventId", booking.getEventId());
            params.addValue("seatIds", booking.getSeatIds());
            params.addValue("totalAmount", booking.getTotalAmount());
            params.addValue("subtotal", booking.getSubtotal());
            params.addValue("expiredAt", booking.getExpiredAt());
            params.addValue("status", booking.getStatus().name());
            params.addValue("paymentMethod", booking.getPaymentMethod().name());

            if (booking.getDiscountAmount() != null) {
                params.addValue("discountAmount", booking.getDiscountAmount());
            } else {
                params.addValue("discountAmount", null);
            }
            if (booking.getDiscountCode() != null) {
                params.addValue("discountCode", booking.getDiscountCode());
            } else {
                params.addValue("discountCode", null);
            }
            if (booking.getMomoOrderId() != null) {
                params.addValue("momoOrderId", booking.getMomoOrderId());
            } else {
                params.addValue("momoOrderId", null);
            }
            if (booking.getMomoTransactionId() != null) {
                params.addValue("momoTransactionId", booking.getMomoTransactionId());
            } else {
                params.addValue("momoTransactionId", null);
            }
            if (booking.getPaymentUrl() != null) {
                params.addValue("paymentUrl", booking.getPaymentUrl());
            } else {
                params.addValue("paymentUrl", null);
            }
            if (booking.getPaymentCode() != null) {
                params.addValue("paymentCode", booking.getPaymentCode());
            } else {
                params.addValue("paymentCode", null);
            }
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});

            Long bookingId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            logger.info("{}|Success|bookingId={}" + logPrefix + bookingId);
            return bookingId;
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return null;
    }


    public boolean updateBooking(Booking booking) {
        String logPrefix = "updateBookingDynamic|id=" + booking.getId();
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", booking.getId());

            StringBuilder sql = new StringBuilder("UPDATE bookings SET updated_at = NOW()");

            if (booking.getStatus() != null) {
                sql.append(", status = :status");
                params.addValue("status", booking.getStatus().name());
            }

            if (booking.getPaymentMethod() != null) {
                sql.append(", payment_method = :paymentMethod");
                params.addValue("paymentMethod", booking.getPaymentMethod().name());
            }

            if (booking.getMomoOrderId() != null && !booking.getMomoOrderId().isEmpty()) {
                sql.append(", momo_order_id = :momoOrderId");
                params.addValue("momoOrderId", booking.getMomoOrderId());
            }

            if (booking.getMomoTransactionId() != null && !booking.getMomoTransactionId().isEmpty()) {
                sql.append(", momo_transaction_id = :momoTransactionId");
                params.addValue("momoTransactionId", booking.getMomoTransactionId());
            }

            if (booking.getPaymentUrl() != null && !booking.getPaymentUrl().isEmpty()) {
                sql.append(", payment_url = :paymentUrl");
                params.addValue("paymentUrl", booking.getPaymentUrl());
            }

            if (booking.getPaymentCode() != null && !booking.getPaymentCode().isEmpty()) {
                sql.append(", payment_code = :paymentCode");
                params.addValue("paymentCode", booking.getPaymentCode());
            }

            if (booking.getPaidAt() != null) {
                sql.append(", paid_at = :paidAt");
                params.addValue("paidAt", booking.getPaidAt());
            }

            if (booking.getCancelledAt() != null) {
                sql.append(", cancelled_at = :cancelledAt");
                params.addValue("cancelledAt", booking.getCancelledAt());
            }

            if (booking.getCancellationReason() != null && !booking.getCancellationReason().isEmpty()) {
                sql.append(", cancellation_reason = :cancellationReason");
                params.addValue("cancellationReason", booking.getCancellationReason());
            }

            if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
                sql.append(", notes = :notes");
                params.addValue("notes", booking.getNotes());
            }

            sql.append(" WHERE id = :id");

            int rowsAffected = jdbcTemplate.update(sql.toString(), params);
            return rowsAffected > 0;
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return false;
    }

    public boolean deleteBooking(Long id) {
        String logPrefix = "deleteBooking|id=" + id;
        try {
            String sql = "DELETE FROM bookings WHERE id = :id";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            int rows = jdbcTemplate.update(sql, params);
            return rows > 0;
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
        }
        return false;
    }

    private RowMapper<Booking> bookingRowMapper() {
        return new RowMapper<>() {
            @Override
            public Booking mapRow(ResultSet rs, int rowNum) throws SQLException {
                Booking b = new Booking();
                b.setId(rs.getLong("id"));
                b.setBookingCode(rs.getString("booking_code"));
                b.setUserId(rs.getString("user_id"));
                b.setEventId(rs.getLong("event_id"));
                b.setSeatIds(rs.getString("seat_ids"));
                b.setTotalAmount(rs.getDouble("total_amount"));
                b.setSubtotal(rs.getDouble("subtotal"));
                b.setDiscountAmount(rs.getDouble("discount_amount"));
                b.setDiscountCode(rs.getString("discount_code"));
                b.setStatus(Booking.BookingStatus.valueOf(rs.getString("status")));
                b.setPaymentMethod(Booking.PaymentMethod.valueOf(rs.getString("payment_method")));
                b.setMomoOrderId(rs.getString("momo_order_id"));
                b.setMomoTransactionId(rs.getString("momo_transaction_id"));
                b.setPaymentUrl(rs.getString("payment_url"));
                b.setPaymentCode(rs.getString("payment_code"));
                b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                b.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                b.setExpiredAt(rs.getTimestamp("expired_at").toLocalDateTime());
                if (rs.getTimestamp("paid_at") != null)
                    b.setPaidAt(rs.getTimestamp("paid_at").toLocalDateTime());
                if (rs.getTimestamp("cancelled_at") != null)
                    b.setCancelledAt(rs.getTimestamp("cancelled_at").toLocalDateTime());
                b.setCancellationReason(rs.getString("cancellation_reason"));
                b.setNotes(rs.getString("notes"));
                return b;
            }
        };
    }

}
