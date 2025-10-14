package com.vticket.vticket.service;

import com.vticket.vticket.domain.mysql.entity.Booking;
import com.vticket.vticket.domain.mysql.repo.BookingRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    @Autowired
    private BookingRepo bookingRepo;

    public Booking getBookingByCode(String code) {
        long start = System.currentTimeMillis();
        if (code == null || code.isEmpty()) {
            logger.info("Booking code is null or empty.");
            return null;
        }
        try {
            Booking booking = bookingRepo.getBookingByBookingCode(code);
            if (booking == null) {
                logger.info("No booking found for code {}.", code);
                return null;
            }
            logger.info("Fetched booking for code {} in {} ms.", code, (System.currentTimeMillis() - start));
            return booking;
        } catch (Exception e) {
            logger.error("Error fetching booking by code {}: {}", code, e.getMessage(), e);
            return null;
        }
    }

}
