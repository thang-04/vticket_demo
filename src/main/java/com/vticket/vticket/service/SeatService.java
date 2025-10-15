package com.vticket.vticket.service;

import com.vticket.vticket.config.redis.RedisKey;
import com.vticket.vticket.domain.mongodb.entity.User;
import com.vticket.vticket.domain.mysql.entity.Booking;
import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.entity.TicketType;
import com.vticket.vticket.domain.mysql.repo.BookingRepo;
import com.vticket.vticket.domain.mysql.repo.SeatRepo;
import com.vticket.vticket.dto.request.ListItem;
import com.vticket.vticket.dto.request.SubmitTicketRequest;
import com.vticket.vticket.dto.response.SeatResponse;
import com.vticket.vticket.dto.response.SubmitTicketResponse;

import com.vticket.vticket.dto.response.TicketItemResponse;
import com.vticket.vticket.utils.CommonUtils;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
public class SeatService {
    private static final Logger logger = LogManager.getLogger(SeatService.class);

    private static final long SEAT_HOLD_TTL_MINUTES = 3;

    private static final long BOOKING_TIME_MINUTES = 5;


    @Autowired
    private JwtService jwtService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SeatRepo seatRepo;

    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private UserService userService;


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

            Long existingCount = zset.zCard(key);//count of holders

            //check if already hold
            if (existingCount != null && existingCount > 0) {
                failedSeats.add(seatId);
                continue;
            }
            //add to Redis ZSet
            zset.add(key, "seat_" + seatId, now);
            redis.expire(key, SEAT_HOLD_TTL_MINUTES, TimeUnit.MINUTES);
        }

        if (!failedSeats.isEmpty()) {
            logger.warn("holdSeatsZSet|Failed for seatIds: {}", failedSeats.stream().map(Object::toString).collect(Collectors.joining(",")));
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
            zSetOps.remove(key, "seat_" + seatId);

            //delete if no other holder
            Long size = zSetOps.zCard(key);
            if (size == null || size == 0) {
                redisService.getRedisSsoUser().delete(key);
            }
        }
        logger.info("releaseSeats|Released seats: {}", seatIds);
    }

    public SubmitTicketResponse submitTicket(SubmitTicketRequest request, String paymentCode, String userToken) {
        long start = System.currentTimeMillis();
        String bookingCode = UUID.randomUUID().toString();

        try {
            Double totalAmount = 0.0;
            List<Seat> seats = seatRepo.getSeatsByIds(request.getListItem().stream()
                    .map(ListItem::getSeatId)
                    .collect(Collectors.toList()));

            if (CollectionUtils.isEmpty(seats) || seats.size() != request.getListItem().size()) {
                logger.warn("submitTicket|Some seats not found in DB");
                return null;
            }
            //check seat available
            for(Seat seat : seats){
                if(!seat.getStatus().equals(Seat.SeatStatus.AVAILABLE)){
                    logger.warn("submitTicket|Some seats not available");
                    return null;
                }
            }

            for (Seat seat : seats) {
                totalAmount += seat.getPrice();
            }
            List<TicketItemResponse> ticketItems = new ArrayList<>();
            //group by ticket type
            Map<Long, List<Seat>> groupedSeats = seats.stream()
                    .collect(Collectors.groupingBy(s -> s.getTicketType().getId()));
            //build response
            for (Map.Entry<Long, List<Seat>> entry : groupedSeats.entrySet()) {
                TicketType type = entry.getValue().getFirst().getTicketType();
                List<Seat> seatList = entry.getValue();

                TicketItemResponse item = new TicketItemResponse();
                item.setId(type.getId());
                item.setEvent_id(type.getEventId());
                item.setTicket_name(type.getName());
                item.setColor(type.getColor());
                item.setIs_free(type.getIs_free());
                item.setPrice(type.getPrice());
                item.setOriginal_price(type.getOriginal_price());
                item.setIs_discount(type.getIs_discount());
                item.setDiscount_percent(type.getDiscount_percent());
                item.setQuantity(seatList.size());

                List<SeatResponse> seatResponses = seatList.stream().map(seat -> {
                    SeatResponse s = new SeatResponse();
                    s.setId(seat.getId());
                    s.setTicket_type_id(type.getId());
                    s.setSeat_name(seat.getSeat_name());
                    s.setSeat_number(seat.getSeat_number());
                    s.setRow_name(seat.getRow_name());
                    s.setColumn_number(seat.getColumn_number());
                    return s;
                }).collect(Collectors.toList());

                item.setSeats(seatResponses);
                ticketItems.add(item);
            }

//            if (StringUtils.isNotEmpty(request.getDiscountCode())) {
//                BigDecimal discount = totalAmount.multiply(BigDecimal.valueOf(0.1));
//                totalAmount = totalAmount.subtract(discount);
//            }

            SubmitTicketResponse response = new SubmitTicketResponse();
            response.setBookingCode(bookingCode);
            response.setEventId(request.getEventId());
            response.setListItem(ticketItems);
            response.setDiscountCode("");
            response.setPaymentCode(paymentCode);
            response.setSubtotal(totalAmount);
            response.setTotalAmount(totalAmount);
            response.setExpiredAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(BOOKING_TIME_MINUTES));


            //insert booking to DB
            Booking booking = new Booking();
            booking.setBookingCode(bookingCode);
            booking.setUserId(getUserIdFromToken(userToken));//userid or deviceId
            booking.setEventId(request.getEventId());
            booking.setSeatIds(
                    request.getListItem().stream()
                            .map(item -> String.valueOf(item.getSeatId()))
                            .collect(Collectors.joining(","))
            );//join to string
            booking.setSubtotal(totalAmount);
            booking.setTotalAmount(totalAmount);
            booking.setPaymentMethod(Booking.PaymentMethod.MOMO);//default MOMO
            booking.setStatus(Booking.BookingStatus.PENDING);
            booking.setExpiredAt(LocalDateTime.now().plusMinutes(BOOKING_TIME_MINUTES));

            Long bookingId = bookingRepo.createBooking(booking); // return ID insert
            logger.info("submitTicket|Inserted booking with ID: {} for booking code: {}", bookingId, bookingCode);

            response.setBookingId(bookingId);

            logger.info("submitTicket|Success|Booking code: {}|Time: {} ms",
                    bookingCode, (System.currentTimeMillis() - start));

            return response;

        } catch (Exception ex) {
            logger.error("submitTicket|Exception|{}", ex.getMessage(), ex);
        }
        return null;
    }

    public String getUserIdFromToken(String token) {
        try {
            if (!CommonUtils.isNotEmpty(token)) {
                return null;
            }
            User user = userService.getUserFromAccessToken(token);
            if (user != null) {
                return user.getId();
            }else{
                return jwtService.getDeviceId(token);
            }
        } catch (Exception ex) {
            logger.error("getUserIdFromToken|Exception|{}", ex.getMessage(), ex);
            return null;
        }
    }

}
