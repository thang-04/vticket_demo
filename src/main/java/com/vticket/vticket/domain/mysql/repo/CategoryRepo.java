package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.domain.mysql.entity.Category;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CategoryRepo {
    private static final Logger logger = Logger.getLogger(CategoryRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Category> getAllCategories() {
        try {
            String sql = "SELECT category_id, name, description FROM categories ORDER BY name ASC";
            SqlParameterSource params = new MapSqlParameterSource();

            return jdbcTemplate.query(sql, params, new RowMapper<Category>() {
                @Override
                public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Category category = new Category();
                    category.setCategory_id(rs.getLong("category_id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    return category;
                }
            });
        } catch (Exception ex) {
            logger.error("getAllCategories|Exception|{}" + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    public Category getCategoryById(Long categoryId) {
        String logPrefix = "getCategoryById|categoryId=" + categoryId;
        try {
            String sql = "SELECT category_id, name, description FROM categories WHERE category_id = :categoryId";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("categoryId", categoryId);

            return jdbcTemplate.queryForObject(sql, params, new RowMapper<Category>() {
                @Override
                public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Category category = new Category();
                    category.setCategory_id(rs.getLong("category_id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    return category;
                }
            });
        } catch (EmptyResultDataAccessException ex) {
            logger.info("{}|No category found" + logPrefix);
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return null;
    }

    public boolean updateCategory(Category category) {
        String logPrefix = "updateCategory|categoryId=" + category.getCategory_id();
        try {
            String sql = "UPDATE categories SET name = :name, description = :description WHERE category_id = :categoryId";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", category.getName());
            params.addValue("description", category.getDescription());
            params.addValue("categoryId", category.getCategory_id());

            int rowsAffected = jdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (Exception ex) {
            logger.info("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return false;
    }

    public boolean deleteCategory(Long categoryId) {
        String logPrefix = "deleteCategory|categoryId=" + categoryId;
        try {
            String sql = "DELETE FROM categories WHERE category_id = :categoryId";
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("categoryId", categoryId);

            int rowsAffected = jdbcTemplate.update(sql, params);
            return rowsAffected > 0;
        } catch (Exception ex) {
            logger.error("{}|Exception|{}" + logPrefix + ex.getMessage(), ex);
        }
        return false;
    }


}
