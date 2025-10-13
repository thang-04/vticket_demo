package com.vticket.vticket.controller;

import com.vticket.vticket.service.SeatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class MomoController {
    private static final Logger logger = LogManager.getLogger(MomoController.class);

    @Autowired
    private SeatService seatService;



}
