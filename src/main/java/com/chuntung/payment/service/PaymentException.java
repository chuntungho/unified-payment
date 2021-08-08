/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

public class PaymentException extends RuntimeException {
    public PaymentException() {
    }

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(Throwable cause) {
        super(cause);
    }
}
