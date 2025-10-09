package com.vticket.vticket.service;

import com.google.gson.reflect.TypeToken;
import com.vticket.vticket.config.redis.RedisKey;
import com.vticket.vticket.domain.mysql.entity.Event;
import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.repo.SeatRepo;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vticket.vticket.utils.CommonUtils.gson;

@Service
public class SeatService {
    private static final Logger logger = LogManager.getLogger(SeatService.class);
    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeatRepo seatRepo;

    public List<Seat> getListSeat(Long eventId) {
        long start = System.currentTimeMillis();
        if (eventId == null || eventId <= 0) {
            logger.info("getListSeat|Invalid eventId: " + eventId);
            return null;
        }
        try {
            List<Seat> seatList = seatRepo.getAllSeatByEvent(eventId);
            logger.info("getListSeat|eventId=" + eventId + "|size=" + (seatList != null ? seatList.size() : 0)
                    + "|time=" + (System.currentTimeMillis() - start));
            return seatList;
        } catch (Exception ex) {
            logger.error("getEventById|Exception|{}", ex.getMessage(), ex);
        }
        return null;
    }

    public List<Seat> checkHoldSeat(List<Long> seatIds) {
        long start = System.currentTimeMillis();
        List<Seat> heldSeats = new ArrayList<>();

        if (seatIds == null || seatIds.isEmpty()) {
            return heldSeats;
        }

        try {
            String key = RedisKey.SEAT_HOLD + seatIds.toString();
            String resultRedis = (String) redisService.getRedisSsoUser().opsForValue().get(key);

            if (StringUtils.isEmpty(resultRedis)) {
                //get from mysql
                List<Seat> dbSeats = seatRepo.getSeatsByIds(seatIds);
                logger.info("Fetched seats from MySQL for seatIds {}: {} found.", seatIds, dbSeats.size());

                if (!dbSeats.isEmpty()) {
                    for (Seat dbSeat : dbSeats) {
                        Seat seat = new Seat();
                        seat.setId(dbSeat.getId());
                        seat.setEventId(dbSeat.getEventId());
                        seat.setSeat_name(dbSeat.getSeat_name());
                        seat.setRow_name(dbSeat.getRow_name());
                        seat.setSeat_number(dbSeat.getSeat_number());
                        seat.setColumn_number(dbSeat.getColumn_number());
                        seat.setPrice(dbSeat.getPrice());
                        seat.setStatus(Seat.SeatStatus.HOLD);
                        seat.setTicketType(dbSeat.getTicketType());
                        heldSeats.add(seat);
                    }
                    //cache redis
                    redisService.getRedisSsoUser().opsForValue().set(key, gson.toJson(heldSeats));
                    redisService.getRedisSsoUser().expire(key, 5, TimeUnit.MINUTES);
                    logger.info("Stored seat hold status in Redis for seatIds {}: {} seats held.", seatIds, heldSeats.size());
                }

            } else {
                // get from redis
                heldSeats = gson.fromJson(resultRedis, new TypeToken<List<Seat>>() {}.getType());
                logger.info("Fetched seat hold status from Redis for seatIds {}: {} found.", seatIds, heldSeats.size());
            }

            logger.info("checkHoldSeat|seatIds=" + seatIds + "|size=" + heldSeats.size()
                    + "|time=" + (System.currentTimeMillis() - start) + "ms");

            return heldSeats;

        } catch (Exception ex) {
            logger.error("checkHoldSeat|Exception|" + ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }

}
