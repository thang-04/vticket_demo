package com.vticket.vticket.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
        try {
            String test = messageSource.getMessage("email.ticket.subject", null, new Locale("vi", "VN"));
            System.out.println("✓ MessageSource loaded successfully: " + test);
        } catch (Exception e) {
            System.err.println("✗ MessageSource failed to load: " + e.getMessage());
        }
    }

    public String get(String key, Object... args) {
        return messageSource.getMessage(key, args, new Locale("vi", "VN"));
    }
}


