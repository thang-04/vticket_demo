package com.vticket.vticket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    ERROR_CODE_EXCEPTION(-9999, "Unknown error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1009, "Email is not valid", HttpStatus.BAD_REQUEST),
    INVALID_REGISTER(1010, "Invalid register information", HttpStatus.BAD_REQUEST),
    EXPIRED_TOKEN(1011, "Token is expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(1012, "Token is invalid", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(1013, "Refresh token is invalid", HttpStatus.UNAUTHORIZED),
    INVALID_CLIENT(1014, "Client is invalid", HttpStatus.UNAUTHORIZED),
    INVALID_SCOPE(1015, "Scope is invalid", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAIL(1016, "Cannot send OTP", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_OTP(1017, "OTP is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(1018, "OTP is expired", HttpStatus.BAD_REQUEST),
    RATE_LIMIT_EXCEEDED(1019, "Rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_REQUEST(1020, "Invalid request", HttpStatus.BAD_REQUEST),
    SEAT_UNAVAILABLE(2001, "Seat is unavailable", HttpStatus.BAD_REQUEST),
    EVENT_NOT_FOUND(2002, "Event not found", HttpStatus.NOT_FOUND),
    SEAT_NOT_FOUND(2003, "Seat not found", HttpStatus.NOT_FOUND),
    PAYMENT_FAILED(3001, "Payment failed", HttpStatus.PAYMENT_REQUIRED),
    BOOKING_NOT_FOUND(4001, "Booking not found", HttpStatus.NOT_FOUND)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
