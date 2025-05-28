package com.exchange.rates.exception;

/**
 * @author Iman Irajian
 * Date: 5/25/2025 09:20 PM
 */

public class ExchangeRateException extends RuntimeException {
    public ExchangeRateException(String message) {
        super(message);
    }

    public ExchangeRateException(String message, Throwable cause) {
        super(message, cause);
    }
}