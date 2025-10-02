package com.vticket.vticket.controller;

import com.vticket.vticket.domain.mysql.entity.Category;
import com.vticket.vticket.domain.mysql.entity.Event;
import com.vticket.vticket.exception.ErrorCode;
import com.vticket.vticket.service.CategoryService;
import com.vticket.vticket.service.EventService;
import com.vticket.vticket.utils.ResponseJson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
                logger.info("getEvents|Success|Total events: {}|Time taken: {} ms" ,events.size() , (System.currentTimeMillis() - start));
                return ResponseJson.success("danh sach events", events);
            } else {
                logger.info("No events retrieved.");
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No events found");
            }

        } catch (Exception ex) {
            logger.error("getEvents|Exception|{}" , ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching events");
        }
    }

    @GetMapping("{eventId}")
    public String getEventDetail(@PathVariable Long eventId) {
        long start = System.currentTimeMillis();
        try {
            Event event = eventService.getEventById(eventId);
            if (event != null) {
                logger.info("getEventDetail|Success|Event ID: {}|Time taken: {} ms" , eventId , (System.currentTimeMillis() - start));
                return ResponseJson.success("Detail event", event);
            } else {
                logger.info("No event found with ID:{} |Time taken: {}ms" , eventId, (System.currentTimeMillis() - start));
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No event found with the given ID");
            }
        } catch (Exception ex) {
            logger.error("getEventDetail|Exception|{}" , ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while fetching event details");
        }
    }

    @GetMapping("/search")
    public String getEventsByCategoryId(@RequestParam("cateId") String categoryId) {
        long start = System.currentTimeMillis();
        try{
            //hanlde multi id
            List<Long> categoryIds = Arrays.stream(categoryId.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();

            List<Event> events = eventService.getEventsByCategoryId(categoryIds);
            if (events != null) {
                logger.info("searchEvents|Success|Category ID: {}|Total events: {}|Time taken: {} ms" , categoryId, events.size(), (System.currentTimeMillis() - start));
                return ResponseJson.success("Search events by category", events);
            } else {
                logger.info("No events found for Category ID:{} |Time taken: {}ms" , categoryId, (System.currentTimeMillis() - start));
                return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "No events found for the given category");
            }
        } catch (Exception ex) {
            logger.error("searchEvents|Exception|{}" , ex.getMessage(), ex);
            return ResponseJson.of(ErrorCode.ERROR_CODE_EXCEPTION, "An error occurred while searching for events");
        }

    }

}
