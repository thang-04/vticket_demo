package com.vticket.vticket.domain.mongodb.repo;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.service.UserService;
import io.micrometer.common.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class UserCollection {
    private static final Logger logger = LogManager.getLogger(UserCollection.class);

    @Autowired
    @Qualifier("mongoTemplate")
    private MongoTemplate mongoTemplate;

    public User insertUser(User user) {
        mongoTemplate.insert(user);
        return user;
    }

    public User getUserInfoByUserName(String userName) {
        User users = null;
        try {
            Criteria criteria = Criteria.where("username").is(userName);
            Query query = new Query();
            query.addCriteria(criteria);
            users = mongoTemplate.findOne(query, User.class);
        } catch (Exception e) {

        }
        return users;
    }

    public List<User> getAllUsers() {
        return mongoTemplate.findAll(User.class);
    }

    public User getUserById(String userId) {
        User users = null;
        try {
            Criteria criteria = Criteria.where("_id").is(userId);
            Query query = new Query();
            query.addCriteria(criteria);
            users = mongoTemplate.findOne(query, User.class);
            if (users != null) {
                logger.info("Found user by ID: {} - Username: {}", userId, users.getUsername());
            } else {
                logger.warn("User not found with ID: {}", userId);
            }
        } catch (Exception e) {

            logger.error("Error finding user by ID: {} - {}", userId, e.getMessage(), e);
        }
        return users;
    }

    public boolean updateTokenOfUser(User users, Date expireDate) {
        boolean response = false;
        try {
            String userId = users.getId();
            if (StringUtils.isNotEmpty(userId)) {
                Query query = new Query(Criteria.where("id").is(userId));
                Update update = new Update();
                if (users.getAccess_token() != null) {
                    update.set("access_token", users.getAccess_token());
                }
                if (users.getRefresh_token() != null) {
                    update.set("refresh_token", users.getRefresh_token());
                }

                update.set("updated_at", new Date());
                mongoTemplate.updateFirst(query, update, User.class);
                response = true;
            }
        } catch (Exception e) {
            logger.error("updateTokenOfUser|users={}|Exception={}", users, e.getMessage(), e);
        }
        return response;
    }

}
