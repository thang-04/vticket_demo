package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.entity.TicketType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class SeatRepo {

    private static final Logger logger = Logger.getLogger(SeatRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;


    public List<Seat> getAllSeatByEvent(Long eventId) {
        String logPrefix = "getAllSeatByEvent|eventId=" + eventId;

        try {
            String sql = """
                    SELECT s.id, s.event_id, s.seat_name, s.row_name, s.seat_number, s.column_number,
                           s.status, s.price, s.ticket_type_id,
                           t.name AS ticket_type_name, t.color AS ticket_type_color, t.price AS ticket_type_price
                    FROM seats s
                    LEFT JOIN ticket_types t ON s.ticket_type_id = t.id
                    WHERE s.event_id = :eventId and s.status != :status
                    ORDER BY s.row_name, s.seat_number
                    """;

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("status", Seat.SeatStatus.SOLD.ordinal());

            return jdbcTemplate.query(sql, params, seatRowMapper());

        } catch (EmptyResultDataAccessException ex) {
            logger.warn(logPrefix + "|No seats found for this event");
            return List.of();
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
            return List.of();
        }
    }


    public Seat getSeatById(Long seatId) {
        String logPrefix = "getSeatById|seatId=" + seatId;

        try {
            String sql = """
                    SELECT s.id, s.event_id, s.seat_name, s.row_name, s.seat_number, s.column_number,
                           s.status, s.price, s.ticket_type_id,
                           t.name AS ticket_type_name, t.color AS ticket_type_color, t.price AS ticket_type_price
                    FROM seats s
                    LEFT JOIN ticket_types t ON s.ticket_type_id = t.id
                    WHERE s.id = :seatId
                    """;

            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("seatId", seatId);

            return jdbcTemplate.queryForObject(sql, params, seatRowMapper());

        } catch (EmptyResultDataAccessException ex) {
            logger.warn(logPrefix + "|No seat found");
            return null;
        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
            return null;
        }
    }

    public boolean updateSeatStatus(Long seatId, Seat.SeatStatus status) {
        String logPrefix = "updateSeatStatus|seatId=" + seatId + "|status=" + status;
        try {
            String sql = "UPDATE seats SET status = :status WHERE id = :seatId";
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("status", status.ordinal())
                    .addValue("seatId", seatId);

            int rowsAffected = jdbcTemplate.update(sql, params);

            if (rowsAffected > 0) {
                logger.info(logPrefix + "|Seat status updated successfully");
                return true;
            } else {
                logger.warn(logPrefix + "|No seat found to update");
                return false;
            }

        } catch (Exception ex) {
            logger.error(logPrefix + "|Exception|" + ex.getMessage(), ex);
            return false;
        }
    }

    public List<Seat> getSeatsByIds(List<Long> seatIds) {
        String sql = """
        SELECT s.*, t.name AS ticket_type_name, t.color AS ticket_type_color, t.price AS ticket_type_price
        FROM seats s
        LEFT JOIN ticket_types t ON s.ticket_type_id = t.id
        WHERE s.id IN (:seatIds)
        """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("seatIds", seatIds);
        return jdbcTemplate.query(sql, params, seatRowMapper());
    }


    private RowMapper<Seat> seatRowMapper() {
        return (ResultSet rs, int rowNum) -> {
            Seat seat = new Seat();
            seat.setId(rs.getLong("id"));
            seat.setEventId(rs.getLong("event_id"));
            seat.setSeat_name(rs.getString("seat_name"));
            seat.setRow_name(rs.getString("row_name"));
            seat.setSeat_number(rs.getInt("seat_number"));
            seat.setColumn_number(rs.getInt("column_number"));
            seat.setPrice(rs.getDouble("price"));

            // Enum mapping
            int statusOrdinal = rs.getInt("status");
            if (statusOrdinal >= 0 && statusOrdinal < Seat.SeatStatus.values().length) {
                seat.setStatus(Seat.SeatStatus.values()[statusOrdinal]);
            }

            // Mapping TicketType
            TicketType ticketType = new TicketType();
            ticketType.setId(rs.getLong("ticket_type_id"));
            ticketType.setName(rs.getString("ticket_type_name"));
            ticketType.setColor(rs.getString("ticket_type_color"));
            ticketType.setPrice(rs.getDouble("ticket_type_price"));
            seat.setTicketType(ticketType);

            return seat;
        };
    }


}
