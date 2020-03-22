package com.chuntung.payment.service;


import com.chuntung.payment.dto.*;

public interface PaymentVendor {

    FormResult<?> preparePay(PayReq req);

    PayQueryResult queryPay(PayQueryReq req);

    void closePay(PayQueryReq req);

    void refund(RefundReq req);

    RefundQueryResult queryRefund(RefundQueryReq req);
}
