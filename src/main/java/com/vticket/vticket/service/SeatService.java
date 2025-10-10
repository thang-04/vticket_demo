package com.vticket.vticket.service;

import com.google.gson.reflect.TypeToken;
import com.vticket.vticket.config.redis.RedisKey;
import com.vticket.vticket.domain.mysql.entity.Event;
import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.repo.SeatRepo;
import io.micrometer.common.util.StringUtils;
import jakarta.transaction.Transactional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.vticket.vticket.utils.CommonUtils.gson;

@Service
public class SeatService {
    private static final Logger logger = LogManager.getLogger(SeatService.class);

    private static final long SEAT_HOLD_TTL_MINUTES = 3;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeatRepo seatRepo;

    public List<Seat> getListSeat(Long eventId) {
        long start = System.currentTimeMillis();
        try {
            List<Seat> seatList = seatRepo.getAllSeatByEvent(eventId);
            if (seatList == null) return List.of();

            var redis = redisService.getRedisSsoUser();

            for (Seat seat : seatList) {
                String key = RedisKey.SEAT_HOLD + eventId + "_" + seat.getId();

                if (redis.hasKey(key)) {
                    Set<String> first = redis.opsForZSet().range(key, 0, 0);
                    if (first != null && !first.isEmpty()) {
                        //config status in response
                        seat.setStatus(Seat.SeatStatus.HOLD);
                    }
                }
            }
            logger.info("getListSeat|eventId=" + eventId + "|size=" + seatList.size()
                    + "|time=" + (System.currentTimeMillis() - start) + "ms");

            return seatList;
        } catch (Exception ex) {
            logger.error("getListSeat|Exception|" + ex.getMessage(), ex);
            return List.of();
        }
    }

    public List<Long> getHoldSeats(Long eventId, List<Long> seatIds) {
        var redis = redisService.getRedisSsoUser();
        List<Long> held = new ArrayList<>();

        for (Long seatId : seatIds) {
            String key = RedisKey.SEAT_HOLD + eventId + "_" + seatId;
            if (!redis.hasKey(key)) continue;

            Set<String> first = redis.opsForZSet().range(key, 0, 0);
            if (first != null && !first.isEmpty()) {
                held.add(seatId);
            }
        }
        return held;
    }

    public boolean holdSeatsZSet(Long eventId, List<Long> seatIds) {
        var redis = redisService.getRedisSsoUser();
        var zset = redis.opsForZSet();
        List<Long> failedSeats = new ArrayList<>();

        for (Long seatId : seatIds) {
            String key = RedisKey.SEAT_HOLD + eventId + "_" + seatId;
            long now = System.currentTimeMillis();

            //check if already hold
            Long existingCount = zset.zCard(key);

            if (existingCount != null && existingCount > 0) {
                failedSeats.add(seatId);
                continue;
            }
            //add to Redis ZSet
            zset.add(key, "seat_" + seatId, now);
            redis.expire(key, SEAT_HOLD_TTL_MINUTES, TimeUnit.MINUTES);
        }

        if (!failedSeats.isEmpty()) {
            logger.warn("holdSeatsZSet|Failed for seats {}", failedSeats);
            return false;
        }

        logger.info("holdSeatsZSet|Successfully held {} seats for {} minutes",
                seatIds.size(), SEAT_HOLD_TTL_MINUTES);
        return true;
    }


    public void releaseSeats(Long eventId, List<Long> seatIds) {
        var zSetOps = redisService.getRedisSsoUser().opsForZSet();
        for (Long seatId : seatIds) {
            String key = RedisKey.SEAT_HOLD + eventId + "_" + seatId;
            zSetOps.remove(key, "lock");

            //delete if no other holder
            Long size = zSetOps.zCard(key);
            if (size == null || size == 0) {
                redisService.getRedisSsoUser().delete(key);
            }
        }
        logger.info("releaseSeats|Released seats: {}", seatIds);
    }

}
