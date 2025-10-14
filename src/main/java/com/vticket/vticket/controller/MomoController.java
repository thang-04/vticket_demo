package com.vticket.vticket.controller;

import com.vticket.vticket.domain.mysql.entity.Booking;
import com.vticket.vticket.dto.response.MomoCreationResponse;
import com.vticket.vticket.dto.response.MomoPaymentResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.BookingService;
import com.vticket.vticket.service.MomoService;
import com.vticket.vticket.service.SeatService;
import com.vticket.vticket.utils.ResponseJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class MomoController {
    private static final Logger logger = LogManager.getLogger(MomoController.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private MomoService momoService;

    @PostMapping("/{bookingCode}")
    public String createPayment(@PathVariable String bookingCode) {
        logger.info("Creating payment for booking code: {}", bookingCode);
        MomoCreationResponse responseMomo = null;
        try {
            Booking booking = bookingService.getBookingByCode(bookingCode);
            if (booking == null) {
                logger.error("Booking not found for code: {}", bookingCode);
                return ResponseJson.of(ErrorCode.BOOKING_NOT_FOUND, "Booking not found");
            }
            responseMomo = momoService.createQR(booking);
            if (responseMomo == null) {
                logger.error("Failed to create Momo QR for booking code: {}", bookingCode);
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Failed to create Momo QR");
            }
            return ResponseJson.success("Momo QR created", responseMomo);
        } catch (Exception e) {
            logger.error("Error creating payment for booking code: {}: {}", bookingCode, e.getMessage(), e);
            return ResponseJson.of(ErrorCode.valueOf(responseMomo.getResultCode()), "Error creating payment: " + e.getMessage());
        }
    }


}
