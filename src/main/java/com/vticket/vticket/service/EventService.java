package com.vticket.vticket.service;

import com.google.gson.reflect.TypeToken;
import com.vticket.vticket.config.RedisKey;
import com.vticket.vticket.domain.mysql.entity.Event;
import com.vticket.vticket.domain.mysql.repo.EventRepo;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vticket.vticket.utils.CommonUtils.gson;

@Service
public class EventService {

    private static final Logger logger = LogManager.getLogger(EventService.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EventRepo eventRepo;

    public List<Event> getAllEvents() {
        long start = System.currentTimeMillis();
        List<Event> listEvents = new ArrayList<>();
        try {
            String key = RedisKey.REDIS_LIST_EVENT;
            String resultRedis = (String) redisService.getRedisSsoUser().opsForValue().get(key);

            if (StringUtils.isEmpty(resultRedis)) {
                // get list by SQL
                List<Event> list = eventRepo.getAllEvents();
                logger.info("Fetched events from MySQL: {} events found.", list.size());

                Event event;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Event eventItem : list) {
                        event = new Event();
                        event.setEvent_id(eventItem.getEvent_id());
                        event.setTitle(eventItem.getTitle());
                        event.setDescription(eventItem.getDescription());
                        event.setPrice(eventItem.getPrice());
                        event.setVenue(eventItem.getVenue());
                        event.setStart_time(eventItem.getStart_time());
                        event.setEnd_time(eventItem.getEnd_time());
                        event.setCreated_at(eventItem.getCreated_at());
                        // Copy category if exists
                        if (eventItem.getCategory() != null) {
                            event.setCategory(eventItem.getCategory());
                        }
                        listEvents.add(event);
                    }
                }

                // cache redis
                if (CollectionUtils.isNotEmpty(listEvents)) {
                    redisService.getRedisSsoUser().opsForValue().set(key, gson.toJson(listEvents));
                    redisService.getRedisSsoUser().expire(key, 1, TimeUnit.HOURS);
                    logger.info("Stored events in Redis cache.");
                }
                logger.info("getAllEvents in MySQL|Time taken: {} ms", (System.currentTimeMillis() - start));
                return listEvents;
            } else {
                listEvents = (List<Event>) gson.fromJson(resultRedis, new TypeToken<List<Event>>() {
                }.getType());
                logger.info("getAllEvents in Redis|Time taken: {} ms", (System.currentTimeMillis() - start));
                return listEvents;
            }
        } catch (Exception ex) {
            logger.error("getAllEvents|Exception|{}" + ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }

    public Event getEventById(Long eventId) {
        long start = System.currentTimeMillis();

        if (eventId == null || eventId <= 0) {
            return null;
        }

        String key = String.format(RedisKey.REDIS_EVENT_BY_ID, eventId);
        try {
            String resultRedis = (String) redisService.getRedisSsoUser().opsForValue().get(key);

            if (StringUtils.isEmpty(resultRedis)) {
                // get event by SQL
                Event event = eventRepo.getEventById(eventId);

                if (event != null) {
                    // cache redis
                    redisService.getRedisSsoUser().opsForValue().set(key, gson.toJson(event));
                    redisService.getRedisSsoUser().expire(key, 1, TimeUnit.HOURS);
                    logger.info("Stored event in Redis cache with ID: {}", eventId);
                }

                logger.info("getEventById|Time taken: {} ms", (System.currentTimeMillis() - start));
                return event;
            } else {
                Event event = gson.fromJson(resultRedis, Event.class);
                logger.info("getEvent from Redis cache with ID: {} |Time taken: {} ms", eventId, (System.currentTimeMillis() - start));
                return event;
            }
        } catch (Exception ex) {
            logger.error("getEventById|Exception|{}" , ex.getMessage(), ex);
        }
        return null;
    }

    public List<Event> getEventsByCategory(List<Long> categoryId) {
        long start = System.currentTimeMillis();
        List<Event> listEvents = new ArrayList<>();

        if (categoryId == null || categoryId.isEmpty()) {
            return listEvents;
        }

        try {
            String key = RedisKey.REDIS_LIST_EVENT + "category:" + categoryId;
            String resultRedis = (String) redisService.getRedisSsoUser().opsForValue().get(key);

            if (StringUtils.isEmpty(resultRedis)) {
                // get list by SQL
                List<Event> list = new ArrayList<>();
                for(Long cateId : categoryId) {
                     list = eventRepo.getEventsByCategory(cateId);
                }
                logger.info("Fetched events from MySQL for category {}: {} events found.", categoryId, list.size());

                Event event;
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Event eventItem : list) {
                        event = new Event();
                        event.setEvent_id(eventItem.getEvent_id());
                        event.setTitle(eventItem.getTitle());
                        event.setDescription(eventItem.getDescription());
                        event.setPrice(eventItem.getPrice());
                        event.setVenue(eventItem.getVenue());
                        event.setStart_time(eventItem.getStart_time());
                        event.setEnd_time(eventItem.getEnd_time());
                        event.setCreated_at(eventItem.getCreated_at());
                        // Copy category if exists
                        if (eventItem.getCategory() != null) {
                            event.setCategory(eventItem.getCategory());
                        }
                        listEvents.add(event);
                    }
                }

                // cache redis
                if (CollectionUtils.isNotEmpty(listEvents)) {
                    redisService.getRedisSsoUser().opsForValue().set(key, gson.toJson(listEvents));
                    redisService.getRedisSsoUser().expire(key, 1, TimeUnit.HOURS);
                    logger.info("Stored events for category {} in Redis cache.", categoryId);
                }
            } else {
                listEvents = (List<Event>) gson.fromJson(resultRedis, new TypeToken<List<Event>>() {
                }.getType());
                logger.info("Fetched events from Redis cache for category {}: {} events found.", categoryId, listEvents.size());
            }

            logger.info("getEventsByCategory|Time taken: {} ms", (System.currentTimeMillis() - start));
            return listEvents;
        } catch (Exception ex) {
            logger.error("getEventsByCategory|Exception|{}" , ex.getMessage(), ex);
        }
        return new ArrayList<>();
    }
}
