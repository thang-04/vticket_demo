package com.vticket.vticket.domain.mongodb.repo;

import com.vticket.vticket.domain.mongodb.entity.Role;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class RoleCollection {
    private static final Logger logger = LogManager.getLogger(RoleCollection.class);

    @Autowired
    @Qualifier("mongoTemplate")
    private MongoTemplate mongoTemplate;

    public Role getRoleByName(String name) {
        Role role = null;
        try {
            if (name == null || name.isEmpty()) {
                return null;
            }
            Criteria criteria = Criteria.where("name").is(name);
            Query query = new Query();
            query.addCriteria(criteria);
            role = mongoTemplate.findOne(query, Role.class);
            logger.info("getRoleByName: {}", role);
        } catch (Exception e) {
            logger.error("Error getting role by name: {} - {}", name, e.getMessage(), e);
        }
        return role;
    }
}
