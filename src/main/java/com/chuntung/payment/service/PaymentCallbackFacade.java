package com.chuntung.payment.service;

import com.chuntung.payment.service.impl.wxpay.WXPaymentVendor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PaymentCallbackFacade {
    @Resource
    private WXPaymentVendor wxPaymentVendor;

    public String wxpayCallback(String result) {
        return wxPaymentVendor.payCallback(result);
    }

    public String wxpayRefundCallback(String encryptedText) {
        return wxPaymentVendor.refundCallback(encryptedText);
    }
}
