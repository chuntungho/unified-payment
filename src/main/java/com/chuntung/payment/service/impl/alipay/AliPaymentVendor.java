package com.chuntung.payment.service.impl.alipay;

import com.chuntung.payment.service.PaymentVendor;
import com.chuntung.payment.dto.*;

public class AliPaymentVendor implements PaymentVendor {
    @Override
    public FormResult<?> preparePay(PayReq req) {
        return null;
    }

    @Override
    public PayQueryResult queryPay(PayQueryReq req) {
        return null;
    }

    @Override
    public void closePay(PayQueryReq req) {

    }

    @Override
    public void refund(RefundReq req) {

    }

    @Override
    public RefundQueryResult queryRefund(RefundQueryReq req) {
        return null;
    }
}
