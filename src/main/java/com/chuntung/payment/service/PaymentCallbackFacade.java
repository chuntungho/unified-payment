/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import com.chuntung.payment.service.impl.alipay.AliPaymentVendor;
import com.chuntung.payment.service.impl.wxpay.WXPaymentVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class PaymentCallbackFacade {
    private static final Logger logger = LoggerFactory.getLogger(PaymentCallbackFacade.class);

    @Resource
    private WXPaymentVendor wxPaymentVendor;

    @Resource
    private AliPaymentVendor aliPaymentVendor;

    public String wxpayCallback(String result) {
        logger.info("WXPay result: {}", result);
        return wxPaymentVendor.payCallback(result);
    }

    public String wxpayRefundCallback(String encryptedText) {
        logger.info("WXPay refund result: {}", encryptedText);
        return wxPaymentVendor.refundCallback(encryptedText);
    }

    public String alipayCallback(Map<String, String> params) {
        logger.info("AliPay result: {}", params);
        return aliPaymentVendor.payCallback(params);
    }

    public String alipayRefundCallback(Map<String, String> params) {
        logger.info("AliPay refund result: {}", params);
        return aliPaymentVendor.refundCallback(params);
    }
}
