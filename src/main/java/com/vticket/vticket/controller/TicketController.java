package com.vticket.vticket.controller;

import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.SeatService;
import com.vticket.vticket.utils.ResponseJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private static final Logger logger = LogManager.getLogger(TicketController.class);

    @Autowired
    private SeatService seatService;

    @GetMapping("/list")
    public String getTickets(@RequestParam(required = false) Long eventId) {
        long start = System.currentTimeMillis();
        try {
            List<Seat> listSeat = seatService.getListSeat(eventId);
            if(listSeat!= null){
                logger.info("List Seat size: " + listSeat.size() + ", Time taken: " + (System.currentTimeMillis() - start) + "ms");
                return ResponseJson.success("List Seat", listSeat);
            } else {
                logger.info("No seats retrieved.");
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No seats found");
            }
        }catch (Exception ex) {
            logger.error("getListSeats|Exception|{}" , ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching seats");
        }
    }
}
