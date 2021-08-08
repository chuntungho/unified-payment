/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import com.chuntung.payment.service.impl.wxpay.WXPaymentVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PaymentCallbackFacade {
    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackFacade.class);

    @Resource
    private WXPaymentVendor wxPaymentVendor;

    public String wxpayCallback(String result) {
        logger.info("WXPay result: {}", result);
        return wxPaymentVendor.payCallback(result);
    }

    public String wxpayRefundCallback(String encryptedText) {
        logger.info("WXPay refund result: {}", encryptedText);
        return wxPaymentVendor.refundCallback(encryptedText);
    }
}
