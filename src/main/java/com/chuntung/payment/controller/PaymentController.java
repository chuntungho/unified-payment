package com.chuntung.payment.controller;

import com.chuntung.payment.service.PaymentCallbackFacade;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController("payment")
public class PaymentController {
    @Resource
    private PaymentCallbackFacade paymentCallbackFacade;

    @PostMapping("callback/wxpay")
    public String wxpayCallback(@RequestBody String respXml) {
        return paymentCallbackFacade.wxpayCallback(respXml);
    }

    @PostMapping("callback/wxpayRefund")
    public String wxpayRefundCallback(@RequestBody String respXml) {
        return paymentCallbackFacade.wxpayRefundCallback(respXml);
    }
}
