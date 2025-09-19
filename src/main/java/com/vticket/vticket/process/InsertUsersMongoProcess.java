package com.vticket.vticket.process;

import com.viettel.mmserver.base.ProcessThreadMX;
import com.vticket.vticket.domain.mongodb.entity.User;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class InsertUsersMongoProcess extends ProcessThreadMX {

    private LinkedBlockingQueue queue;
    private MongoTemplate mongoTemplate;

    public InsertUsersMongoProcess(String threadName) {
        super(threadName);
    }

    @Override
    protected void process() {
        buStartTime = new Date();
        try {
            long start = System.currentTimeMillis();
            List<User> listUsers = new ArrayList<>();
            //check 500 record or 500ms
            while (listUsers.size() < 500 && System.currentTimeMillis() - start < 500) {
                Object obj = queue.poll();
                if (obj != null && obj instanceof User) {
                    User callLog = (User) obj;
                    listUsers.add(callLog);
                } else {
                    Thread.sleep(1);
                }
            }

            if (listUsers.isEmpty()) {
                Thread.sleep(1);
            } else {
                List<User> listUsersIns = new ArrayList<>();
                for (User user : listUsers) {
                    user.setActive(true);
                    user.setCreated_at(new Date());
                    listUsersIns.add(user);
                }

                // insert to mongo
                BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, User.class);
                bulkOps.insert(listUsersIns);
                bulkOps.execute();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        buStartTime = null;
    }

    public LinkedBlockingQueue getQueue() {
        return queue;
    }

    public void setQueue(LinkedBlockingQueue queue) {
        this.queue = queue;
    }

    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
