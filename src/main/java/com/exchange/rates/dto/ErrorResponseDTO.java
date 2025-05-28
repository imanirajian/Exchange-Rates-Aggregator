package com.exchange.rates.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 * Standard error response structure for API error handling.
 * Contains details about the error that occurred.
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponseDTO {
    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error message describing what went wrong
     */
    private String message;

    /**
     * Timestamp when the error occurred (in epoch milliseconds)
     */
    private long timestamp;

    /**
     * Optional field for additional error details
     */
    private String details;

    /**
     * Optional field for error code (can be used for client-side handling)
     */
    private String errorCode;

    /**
     * Optional field for the path where the error occurred
     */
    private String path;

    /**
     * Factory method to create an ErrorResponse with current timestamp
     *
     * @param status  HTTP status code
     * @param message Error message
     * @return ErrorResponse instance
     */
    public static ErrorResponseDTO of(int status, String message) {
        return ErrorResponseDTO.builder()
                .status(status)
                .message(message)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }

    /**
     * Factory method to create an ErrorResponse with current timestamp and details
     *
     * @param status  HTTP status code
     * @param message Error message
     * @param details Additional error details
     * @return ErrorResponse instance
     */
    public static ErrorResponseDTO of(int status, String message, String details) {
        return ErrorResponseDTO.builder()
                .status(status)
                .message(message)
                .details(details)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}