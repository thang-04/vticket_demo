package com.vticket.vticket.config;

import com.vticket.vticket.domain.mongodb.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class RoleSeeder implements CommandLineRunner {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) {
        if (!mongoTemplate.collectionExists(Role.class)) {
            mongoTemplate.createCollection(Role.class);
        }

        Role admin = mongoTemplate.findById("ADMIN", Role.class);
        if (admin == null) {
            mongoTemplate.insert(Role.builder()
                    .name("ADMIN")
                    .description("Administrator role")
                    .build(), "roles");
        }

        Role user = mongoTemplate.findById("USER", Role.class);
        if (user == null) {
            mongoTemplate.insert(Role.builder()
                    .name("USER")
                    .description("Default user role")
                    .build(), "roles");
        }

    }
}
