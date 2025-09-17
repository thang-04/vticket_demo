package com.vticket.vticket.domain.mysql.repo;

import com.vticket.vticket.domain.mysql.entity.Category;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepo {
    private static final Logger logger = Logger.getLogger(CategoryRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public List<Category> getListCategories() {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, name, description FROM categories");
            List<Category> categories = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                Category category = new Category();
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                return category;
            });
            return categories;
        } catch (Exception e) {
            logger.error("Error fetching categories: " + e.getMessage(), e);
            return null;
        }
    }


}
