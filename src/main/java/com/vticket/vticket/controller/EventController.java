package com.vticket.vticket.controller;

import com.google.gson.Gson;
import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mysql.entity.Category;
import com.vticket.vticket.domain.mysql.entity.Event;
import com.vticket.vticket.domain.mysql.entity.Seat;
import com.vticket.vticket.domain.mysql.repo.SeatRepo;
import com.vticket.vticket.dto.request.ListItem;
import com.vticket.vticket.dto.request.SubmitTicketRequest;

import com.vticket.vticket.dto.response.SeatStatusMessage;
import com.vticket.vticket.dto.response.SubmitTicketResponse;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.*;
import com.vticket.vticket.utils.ResponseJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/event")
public class EventController {
    private static final Logger logger = LogManager.getLogger(EventController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EventService eventService;

    @Autowired
    private SeatService seatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private Gson gson;


    @GetMapping("/categories")
    public String getCategories() {
        List<Category> categories = categoryService.getAllCategories();
        if (categories != null) {
            logger.info("Number of categories retrieved: " + categories.size());
            return ResponseJson.success("danh sach categories", categories);

        } else {
            logger.info("No categories retrieved.");
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No categories found");
        }
    }

    @GetMapping("/list")
    public String getListEvents() {
        long start = System.currentTimeMillis();
        try {
            List<Event> events = eventService.getAllEvents();
            if (events != null) {
                logger.info("getEvents|Success|Total events: {}|Time taken: {} ms", events.size(), (System.currentTimeMillis() - start));
                return ResponseJson.success("danh sach events", events);
            } else {
                logger.info("No events retrieved.");
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No events found");
            }

        } catch (Exception ex) {
            logger.error("getEvents|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching events");
        }
    }

    @GetMapping("/{eventId}")
    public String getEventDetail(@PathVariable Long eventId) {
        long start = System.currentTimeMillis();
        try {
            Event event = eventService.getEventById(eventId);
            if (event != null) {
                logger.info("getEventDetail|Success|Event ID: {}|Time taken: {} ms", eventId, (System.currentTimeMillis() - start));
                return ResponseJson.success("Detail event", event);
            } else {
                logger.info("No event found with ID:{} |Time taken: {}ms", eventId, (System.currentTimeMillis() - start));
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No event found with the given ID");
            }
        } catch (Exception ex) {
            logger.error("getEventDetail|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching event details");
        }
    }

    @GetMapping("/search")
    public String getEventsByCategoryId(@RequestParam("cateId") String categoryId) {
        long start = System.currentTimeMillis();
        try {
            //hanlde multi id
            List<Long> categoryIds = Arrays.stream(categoryId.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();

            List<Event> events = eventService.getEventsByCategoryId(categoryIds);
            if (events != null) {
                logger.info("searchEvents|Success|Category ID: {}|Total events: {}|Time taken: {} ms", categoryId, events.size(), (System.currentTimeMillis() - start));
                return ResponseJson.success("Search events by category", events);
            } else {
                logger.info("No events found for Category ID:{} |Time taken: {}ms", categoryId, (System.currentTimeMillis() - start));
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No events found for the given category");
            }
        } catch (Exception ex) {
            logger.error("searchEvents|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while searching for events");
        }
    }

    @GetMapping("/{eventId}/seat-chart")
    public String getListSeats(@PathVariable("eventId") Long eventId) {
        long start = System.currentTimeMillis();
        try {
            List<Seat> listSeat = seatService.getListSeat(eventId);
            if (listSeat != null) {
                logger.info("List Seat size: " + listSeat.size() + ", Time taken: " + (System.currentTimeMillis() - start) + "ms");
                return ResponseJson.success("List Seat", listSeat);
            } else {
                logger.info("No seats retrieved.");
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No seats found");
            }
        } catch (Exception ex) {
            logger.error("getListSeats|Exception|{}", ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching seats");
        }
    }

    @GetMapping("/{eventId}/seat/{seatId}/check")
    public String checkSeatHold(@PathVariable Long eventId, @PathVariable String seatId) {
        try {
            List<Long> seatIds = Arrays.stream(seatId.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();

            List<Long> heldSeats = seatService.getHoldSeats(eventId, seatIds);

            if (!heldSeats.isEmpty()) {
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                        "Some seats are currently held by another user", heldSeats);
            }
            return ResponseJson.success("All seats available");
        } catch (Exception ex) {
            logger.error("checkSeatHold|Exception|" + ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error checking seat hold");
        }
    }

    @PostMapping("/{eventId}/seat/{seatId}/hold")
    public String holdSeat(@PathVariable Long eventId,
                           @PathVariable String seatId) {
        try {
            List<Long> seatIds = Arrays.stream(seatId.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();

            boolean success = seatService.holdSeatsZSet(eventId, seatIds);

            if (success) {
                for (Long seat_id : seatIds) {
                    //send to broker client subscribe
                    messagingTemplate.convertAndSend(
                            "/topic/seat/" + eventId,
                            new SeatStatusMessage(seat_id, eventId, "HOLD")
                    );
                }
                return ResponseJson.success("Seats held successfully");
            } else {
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                        "Some seats already held by another user");
            }

        } catch (Exception ex) {
            logger.error("holdSeat|Exception|" + ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while holding seats");
        }
    }

    @PostMapping("/booking/submit-ticket")
    public String submitTicket(@RequestBody SubmitTicketRequest request,
                               @RequestHeader("Authorization") String accessToken) {
        logger.info("Received ticket submission request: {}", request);
        if (accessToken != null) {
            accessToken = accessToken.replaceFirst("^Bearer\\s+", "");
        } else {
            return ResponseJson.of(ErrorCode.UNAUTHENTICATED, "Missing Authorization header");
        }
        try {
            List<Long> seatIds = request.getListItem()
                    .stream().map(ListItem::getSeatId)
                    .toList();

            List<Seat> listInvalidSeats = seatService.getHoldSeats(request.getEventId(), seatIds)
                    .stream()
                    .map(id -> {
                        return seatRepo.getSeatById(id);
                    }).toList();
            //hold seat
            boolean success = seatService.holdSeatsZSet(request.getEventId(), seatIds);

            if (!success) {
                logger.info("submitTicket|Failed to hold seats: {}", gson.toJson(listInvalidSeats));
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE,
                        "Some seats already held by another user ", listInvalidSeats);
            }

            SubmitTicketResponse response = seatService.submitTicket(request, Config.PAYMENT_TYPE.MOMO, accessToken);
            logger.info("Received ticket submission response: " + response);
            if (response == null) {
                return ResponseJson.of(ErrorCode.SEAT_UNAVAILABLE, "Ticket submission failed");
            }
            return ResponseJson.success("Ticket submitted successfully", response);
        } catch (Exception ex) {
            logger.error("submitTicket|Exception|" + ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "Error while submitting ticket");
        }
    }

}
