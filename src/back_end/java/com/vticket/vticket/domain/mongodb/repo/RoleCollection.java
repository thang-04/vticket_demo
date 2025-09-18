package com.vticket.vticket.domain.mongodb.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RoleCollection {
    @Autowired
    @Qualifier("mongoTemplate")
    private MongoTemplate mongoTemplate;

}
