package com.vticket.vticket.service;

import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.repo.SeatRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private static final Logger logger = LogManager.getLogger(SeatService.class);
    @Autowired
    private JwtService jwtService;

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

}
