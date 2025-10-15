package com.vticket.vticket.service;

import com.vticket.vticket.domain.mysql.entity.Booking;
import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.repo.BookingRepo;
import com.vticket.vticket.domain.mysql.repo.SeatRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private static final Logger logger = LogManager.getLogger(BookingService.class);

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private SeatRepo seatRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SeatService seatService;

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

    public boolean updateBookingStatus(String bookingCode) {
        long start = System.currentTimeMillis();
        if (bookingCode == null || bookingCode.isEmpty()) {
            logger.info("Booking code is null or empty.");
            return false;
        }
        try {
            Booking booking = bookingRepo.getBookingByBookingCode(bookingCode);
            if (booking == null) {
                logger.info("No booking found for code {}.", bookingCode);
                return false;
            }
            if (bookingRepo.updateBookingStatus(booking.getId(), Booking.BookingStatus.PAID)) {
                logger.info("Booking status updated to PAID for code {}.", bookingCode);

                List<Long> ids = Arrays.stream(booking.getSeatIds().split(","))
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                // Release the seats
                seatService.releaseSeats(booking.getEventId(), ids);
                //Set seats status
                for(Long id : ids) {
                    seatRepo.updateSeatStatus(id, Seat.SeatStatus.SOLD);
                }
                // Send ticket email
                emailService.sendTicketMail(booking);
                logger.info("Sent ticket email for booking code {}.", bookingCode);
            } else {
                logger.warn("Failed to update booking status for code {}.", bookingCode);
                return false;
            }
            logger.info("Updated booking status for code {} in {} ms.", bookingCode, (System.currentTimeMillis() - start));
            return true;
        } catch (Exception e) {
            logger.error("Error updating booking status by code {}: {}", bookingCode, e.getMessage(), e);
        }
        return false;
    }

}
