/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.controller;

import com.chuntung.payment.dto.*;
import com.chuntung.payment.service.PaymentBridge;
import com.chuntung.payment.service.PaymentCallbackFacade;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("payment")
public class PaymentController {
    @Resource
    private PaymentCallbackFacade paymentCallbackFacade;

    @Resource
    private PaymentBridge paymentBridge;

    @PostMapping("prepare")
    public FormResult preparePay(@RequestBody PayReq<Object> req) {
        return paymentBridge.preparePay(req);
    }

    @PostMapping("query")
    public PayQueryResult queryPay(@RequestBody PayQueryReq req) {
        return paymentBridge.queryPay(req);
    }

    @PostMapping("close")
    public void closePay(@RequestBody PayQueryReq req) {
        paymentBridge.closePay(req);
    }

    @PostMapping("refund")
    public void refund(@RequestBody RefundReq req) {
        paymentBridge.refund(req);
    }

    @PostMapping("queryRefund")
    public RefundQueryResult queryRefund(@RequestBody RefundQueryReq req) {
        return paymentBridge.queryRefund(req);
    }

    @PostMapping("callback/wxpay")
    public String wxpayCallback(@RequestBody String respXml) {
        return paymentCallbackFacade.wxpayCallback(respXml);
    }

    @PostMapping("callback/wxpayRefund")
    public String wxpayRefundCallback(@RequestBody String respXml) {
        return paymentCallbackFacade.wxpayRefundCallback(respXml);
    }
}
