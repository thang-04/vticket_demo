package com.vticket.vticket.domain.mongodb.repo;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.dto.request.UserCreationRequest;
import com.vticket.vticket.dto.request.UserUpdateRequest;
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

import static com.vticket.vticket.utils.CommonUtils.gson;

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
            logger.error("Error finding user by username: {} - {}", userName, e.getMessage(), e);
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
                Query query = new Query(Criteria.where("_id").is(userId));
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

    public boolean updateUserInfo(String userId, UserUpdateRequest req) {
        String logPrefix = "[updateProfile]|profile=" + gson.toJson(req);
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("_id").is(userId));
            Update update = new Update();
            if (StringUtils.isNotEmpty(req.getUsername())) {
                update.set("username", req.getUsername());
            }
            String fullName = "";
            if (StringUtils.isNotEmpty(req.getFirstName())) {
                fullName += req.getFirstName();
            }
            if (StringUtils.isNotEmpty(req.getLastName())) {
                if (!fullName.isEmpty()) {
                    fullName += " ";
                }
                fullName += req.getLastName();
            }
            if (StringUtils.isNotEmpty(fullName)) {
                update.set("full_name", fullName);
            }
            if (StringUtils.isNotEmpty(req.getAddress())) {
                update.set("address", req.getAddress());
            }
            if (StringUtils.isNotEmpty(req.getAvatar())) {
                update.set("avatar", req.getAvatar());
            }
            update.set("updated_at", new Date());
            mongoTemplate.updateFirst(query, update, "users");
            logger.info("{}|Profile updated successfully in 'users'", logPrefix);
            return true;
        } catch (Exception e) {
            logger.error("{}|Exception={}", logPrefix, e.getMessage(), e);
            return false;
        }
    }

    public boolean updateUserDeleteAccount(String userId, String username, String email) {
        logger.info("deleteAccount|userId={}|username={}|email={}", userId, username, email);
        boolean response = false;
        try {
            if (StringUtils.isNotEmpty(userId)) {
                Query query = new Query(Criteria.where("_id").is(userId));
                Update update = new Update();

                if (StringUtils.isNotEmpty(email)) {
                    update.set("email", email);
                }

                if (StringUtils.isNotEmpty(username)) {
                    update.set("username", username);
                }
                update.set("status", false);
                update.set("updated_at", new Date());
                mongoTemplate.updateFirst(query, update, User.class);
                response = true;
            }
        } catch (Exception e) {
            logger.error("deleteAccount|userId={}|Exception={}", userId, e.getMessage(), e);
        }
        return response;
    }

}
