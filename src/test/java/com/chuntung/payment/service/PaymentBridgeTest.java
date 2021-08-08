/*
 * Copyright (c) 2020 Tony Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import com.chuntung.payment.dto.FormResult;
import com.chuntung.payment.dto.PayFromEnum;
import com.chuntung.payment.dto.PayReq;
import com.chuntung.payment.dto.PaymentVendorEnum;
import com.chuntung.payment.dto.wxpay.WXPayParam;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PaymentBridgeTest {
    @Resource
    PaymentBridge paymentBridge;

    @Test
    void preparePay() {
        PayReq<Object> req = new PayReq<>();
        req.setVendor(PaymentVendorEnum.WXPay);
        req.setFrom(PayFromEnum.PC);
        req.setRequestNo("test-request-no");
        req.setAmount(new BigDecimal("1.01"));
        req.setClientIp("local");
        WXPayParam param = new WXPayParam();
        param.setProductId("test-product-id");
        req.setSpecialParam(param);

        FormResult formResult = paymentBridge.preparePay(req);
        assertNotNull(formResult.getFormParam());
    }

    @Test
    void queryPay() {
    }

    @Test
    void closePay() {
    }

    @Test
    void refund() {
    }

    @Test
    void queryRefund() {
    }

    @Test
    void notifyPayResult() {
    }

    @Test
    void notifyRefundResult() {
    }
}