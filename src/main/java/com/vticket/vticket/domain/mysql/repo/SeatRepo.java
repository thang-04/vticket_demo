package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.domain.mysql.entity.Seat;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeatRepo {

    private static final Logger logger = Logger.getLogger(SeatRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Seat> getAllSeatByEvent(Long eventId) {
        String logPrefix = "getAllSeatByEvent|eventId=" + eventId;

        try {
            String sql = "SELECT * FROM seats s" +
                    "LEFT JOIN events e ON e.event_id = s.event_id " +
                    " WHERE e.event_id=:eventId";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("eventId", eventId);
            return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
                Seat seat = new Seat();
                seat.setId(rs.getLong("id"));
                seat.setSeat_column(rs.getString("seat_column"));
                seat.setSeat_row(rs.getString("seat_row"));
                seat.setSeat_type(rs.getString("seat_type"));
                seat.setZone(rs.getString("zone"));
                seat.setPrice(rs.getDouble("price"));
                seat.setStatus(Seat.SeatStatus.valueOf(rs.getString("status")));
                seat.setEvent_id(rs.getLong("event_id"));
                return seat;
            });
        } catch (EmptyResultDataAccessException ex) {
            logger.warn("{}|No event found" + logPrefix);
            return null;
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return null;
    }



    //   public Event getEventById(Long eventId) {
    //        String logPrefix = "getEventById|eventId=" + eventId;
    //        try {
    //            String sql = "SELECT e.event_id, e.title, e.description, e.price, e.venue, " +
    //                    "e.start_time, e.end_time, e.created_at, e.category_category_id, " +
    //                    "c.category_id, c.name as category_name, c.description as category_description " +
    //                    "FROM events e " +
    //                    "LEFT JOIN categories c ON e.category_category_id = c.category_id " +
    //                    "WHERE e.event_id = :eventId";
    //            MapSqlParameterSource params = new MapSqlParameterSource();
    //            params.addValue("eventId", eventId);
    //
    //            return jdbcTemplate.queryForObject(sql, params, new RowMapper<Event>() {
    //                @Override
    //                public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
    //                    Event event = mapEventFromResultSet(rs);
    //
    //                    // Map category if exists
    //                    if (rs.getLong("category_category_id") > 0) {
    //                        Category category = new Category();
    //                        category.setCategory_id(rs.getLong("category_id"));
    //                        category.setName(rs.getString("category_name"));
    //                        category.setDescription(rs.getString("category_description"));
    //                        event.setCategory(category);
    //                    }
    //
    //                    return event;
    //                }
    //            });
    //        } catch (EmptyResultDataAccessException ex) {
    //            logger.warn("{}|No event found" + logPrefix);
    //            return null;
    //        } catch (Exception ex) {
    //            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
    //        }
    //        return null;
    //    }
}
