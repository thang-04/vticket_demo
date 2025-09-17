package com.vticket.vticket.domain.mysql.repo;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepo {

    private static final Logger logger = Logger.getLogger(EventRepo.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
}
