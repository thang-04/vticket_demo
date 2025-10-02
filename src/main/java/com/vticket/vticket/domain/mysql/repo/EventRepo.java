package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.domain.mysql.entity.Category;
import com.vticket.vticket.domain.mysql.entity.Event;
import io.micrometer.common.util.StringUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class EventRepo {

    private static final Logger logger = Logger.getLogger(EventRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Event> getAllEvents() {
        try {
            String sql = "SELECT e.event_id, e.title, e.description, e.price, e.venue, " +
                    "e.start_time, e.end_time, e.created_at, e.category_category_id, " +
                    "c.category_id, c.name as category_name, c.description as category_description " +
                    "FROM events e " +
                    "LEFT JOIN categories c ON e.category_category_id = c.category_id " +
                    "ORDER BY e.created_at DESC";
            SqlParameterSource params = new MapSqlParameterSource();

            return jdbcTemplate.query(sql, params, new RowMapper<Event>() {
                @Override
                public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Event event = mapEventFromResultSet(rs);

                    // Map category if exists
                    if (rs.getLong("category_category_id") > 0) {
                        Category category = new Category();
                        category.setCategory_id(rs.getLong("category_id"));
                        category.setName(rs.getString("category_name"));
                        category.setDescription(rs.getString("category_description"));
                        event.setCategory(category);
                    }

                    return event;
                }
            });
        } catch (Exception ex) {
            logger.error("getAllEvents|Exception|{}" + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }


    public Event getEventById(Long eventId) {
        String logPrefix = "getEventById|eventId=" + eventId;
        try {
            String sql = "SELECT e.event_id, e.title, e.description, e.price, e.venue, " +
                    "e.start_time, e.end_time, e.created_at, e.category_category_id, " +
                    "c.category_id, c.name as category_name, c.description as category_description " +
                    "FROM events e " +
                    "LEFT JOIN categories c ON e.category_category_id = c.category_id " +
                    "WHERE e.event_id = :eventId";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("eventId", eventId);

            return jdbcTemplate.queryForObject(sql, params, new RowMapper<Event>() {
                @Override
                public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Event event = mapEventFromResultSet(rs);

                    // Map category if exists
                    if (rs.getLong("category_category_id") > 0) {
                        Category category = new Category();
                        category.setCategory_id(rs.getLong("category_id"));
                        category.setName(rs.getString("category_name"));
                        category.setDescription(rs.getString("category_description"));
                        event.setCategory(category);
                    }

                    return event;
                }
            });
        } catch (EmptyResultDataAccessException ex) {
            logger.warn("{}|No event found" + logPrefix);
            return null;
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return null;
    }

    public List<Event> getEventsByCategory(Long categoryId) {
        String logPrefix = "getEventsByCategory|categoryId=" + categoryId;
        try {
            String sql = "SELECT e.event_id, e.title, e.description, e.price, e.venue, " +
                    "e.start_time, e.end_time, e.created_at, e.category_category_id , " +
                    "c.category_id, c.name as category_name, c.description as category_description " +
                    "FROM events e " +
                    "LEFT JOIN categories c ON e.category_category_id = c.category_id " +
                    "WHERE e.category_category_id = :categoryId " +
                    "ORDER BY e.start_time ASC";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("categoryId", categoryId);

            return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapEventFromResultSet(rs));
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    public List<Event> getUpcomingEvents() {
        try {
            String sql = "SELECT e.event_id, e.title, e.description, e.price, e.venue, " +
                    "e.start_time, e.end_time, e.created_at, e.category_id, " +
                    "c.name as category_name, c.description as category_description " +
                    "FROM events e " +
                    "LEFT JOIN categories c ON e.category_category_id = c.category_id " +
                    "WHERE e.start_time > NOW() " +
                    "ORDER BY e.start_time ASC";
            SqlParameterSource params = new MapSqlParameterSource();

            return jdbcTemplate.query(sql, params, new RowMapper<Event>() {
                @Override
                public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Event event = mapEventFromResultSet(rs);

                    // Map category if exists
                    if (rs.getLong("category_id") > 0) {
                        Category category = new Category();
                        category.setCategory_id(rs.getLong("category_id"));
                        category.setName(rs.getString("category_name"));
                        category.setDescription(rs.getString("category_description"));
                        event.setCategory(category);
                    }

                    return event;
                }
            });
        } catch (Exception ex) {
            logger.error("getUpcomingEvents|Exception|{}" + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

//    /**
//     * Create new event
//     */
//    public Long createEvent(Event event) {
//        String logPrefix = "createEvent|title=" + event.getTitle();
//        try {
//            String sql = "INSERT INTO events (title, description, price, venue, start_time, end_time, created_at, category_id) " +
//                    "VALUES (:title, :description, :price, :venue, :startTime, :endTime, :createdAt, :categoryId)";
//            MapSqlParameterSource params = new MapSqlParameterSource();
//            params.addValue("title", event.getTitle());
//            params.addValue("description", event.getDescription());
//            params.addValue("price", event.getPrice());
//            params.addValue("venue", event.getVenue());
//            params.addValue("startTime", event.getStart_time());
//            params.addValue("endTime", event.getEnd_time());
//            params.addValue("createdAt", event.getCreated_at() != null ? event.getCreated_at() : new Date());
//            params.addValue("categoryId", event.getCategory() != null ? event.getCategory().getCategory_id() : null);
//
//            return insertAndReturnKey(sql, params);
//        } catch (Exception ex) {
//            logger.error("{}|Exception|{}", logPrefix, ex.getMessage(), ex);
//        }
//        return null;
//    }

    public boolean updateEventDynamic(Event event) {
        String logPrefix = "updateEventDynamic|eventId=" + event.getEvent_id();
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("eventId", event.getEvent_id());

            StringBuilder sql = new StringBuilder("UPDATE events SET updated_at = NOW()");

            if (event.getTitle() != null && !event.getTitle().isEmpty()) {
                sql.append(", title = :title");
                params.addValue("title", event.getTitle());
            }

            if (event.getDescription() != null && !event.getDescription().isEmpty()) {
                sql.append(", description = :description");
                params.addValue("description", event.getDescription());
            }

            if (event.getPrice() != null) {
                sql.append(", price = :price");
                params.addValue("price", event.getPrice());
            }

            if (event.getVenue() != null && !event.getVenue().isEmpty()) {
                sql.append(", venue = :venue");
                params.addValue("venue", event.getVenue());
            }

            if (event.getStart_time() != null) {
                sql.append(", start_time = :startTime");
                params.addValue("startTime", event.getStart_time());
            }

            if (event.getEnd_time() != null) {
                sql.append(", end_time = :endTime");
                params.addValue("endTime", event.getEnd_time());
            }

            if (event.getCategory() != null && event.getCategory().getCategory_id() != null) {
                sql.append(", category_category_id = :categoryId");
                params.addValue("categoryId", event.getCategory().getCategory_id());
            }

            sql.append(" WHERE event_id = :eventId");

            int rowsAffected = jdbcTemplate.update(sql.toString(), params);
            return rowsAffected > 0;
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return false;
    }


    /**
     * Delete event
     */
    public boolean deleteEvent(Long eventId) {
        String logPrefix = "deleteEvent|eventId=" + eventId;
        try {
            String sql = "DELETE FROM events WHERE event_id = :eventId";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("eventId", eventId);

            int rowsAffected = jdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * Search events by title or description
     */
    public List<Event> searchEventsDynamic(String keyword, Long categoryId, Date start, Date end) {
        StringBuilder sql = new StringBuilder(
                "SELECT e.event_id, e.title, e.description, e.price, e.venue, " +
                        "e.start_time, e.end_time, e.created_at, e.category_category_id, " +
                        "c.category_id, c.name as category_name, c.description as category_description " +
                        "FROM events e LEFT JOIN categories c ON e.category_category_id = c.category_id WHERE 1=1"
        );

        MapSqlParameterSource params = new MapSqlParameterSource();

        if (StringUtils.isNotBlank(keyword)) {
            sql.append(" AND (e.title LIKE :keyword OR e.description LIKE :keyword)");
            params.addValue("keyword", "%" + keyword + "%");
        }
        if (categoryId != null) {
            sql.append(" AND e.category_category_id = :categoryId");
            params.addValue("categoryId", categoryId);
        }
        if (start != null) {
            sql.append(" AND e.start_time >= :start");
            params.addValue("start", start);
        }
        if (end != null) {
            sql.append(" AND e.end_time <= :end");
            params.addValue("end", end);
        }

        sql.append(" ORDER BY e.start_time ASC");

        return jdbcTemplate.query(sql.toString(), params, (rs, rowNum) -> mapEventFromResultSet(rs));
    }


    private Event mapEventFromResultSet(ResultSet rs) throws SQLException {
        Event event = new Event();
        event.setEvent_id(rs.getLong("event_id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setPrice(rs.getDouble("price"));
        event.setVenue(rs.getString("venue"));
        event.setStart_time(rs.getTimestamp("start_time"));
        event.setEnd_time(rs.getTimestamp("end_time"));
        event.setCreated_at(rs.getTimestamp("created_at"));

        if (rs.getObject("category_id") != null) {
            Category category = new Category();
            category.setCategory_id(rs.getLong("category_id"));
            category.setName(rs.getString("category_name"));
            category.setDescription(rs.getString("category_description"));
            event.setCategory(category);
        }

        return event;
    }
}
