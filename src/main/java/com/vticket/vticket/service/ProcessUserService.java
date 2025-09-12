package com.vticket.vticket.service;

import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.process.InsertUsersMongoProcess;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;


@Service
public class ProcessUserService {


    @Autowired
    @Qualifier("mongoTemplate")
    private MongoTemplate mongoTemplate;

    private static LinkedBlockingQueue insertUsersQueue;

    private ProcessUserService() {

    }


    public void start() {
        insertUsersQueue = new LinkedBlockingQueue();
        for (int i = 0; i < 5; i++) {
            InsertUsersMongoProcess insertUsersMongoProcess = new InsertUsersMongoProcess("insertUsersProcess_" + i);
            insertUsersMongoProcess.setQueue(insertUsersQueue);
            insertUsersMongoProcess.setMongoTemplate(mongoTemplate);
            insertUsersMongoProcess.start();
        }
    }

    public void enQueueUser(User user) {
        insertUsersQueue.add(user);
    }


}
