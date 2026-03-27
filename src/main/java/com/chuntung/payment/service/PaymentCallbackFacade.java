/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Routes async payment/refund callbacks to the registered {@link CallbackVendor}.
 * No vendor-specific code lives here; new vendors self-register via
 * {@link PaymentBridge#registerCallbackVendor}.
 */
@Component
public class PaymentCallbackFacade {
    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackFacade.class);

    @Resource
    private PaymentBridge paymentBridge;

    public String handlePayCallback(String vendor, String body) {
        logger.info("Pay callback from vendor [{}]", vendor);
        return paymentBridge.handlePayCallback(vendor, body);
    }

    public String handleRefundCallback(String vendor, String body) {
        logger.info("Refund callback from vendor [{}]", vendor);
        return paymentBridge.handleRefundCallback(vendor, body);
    }
}
