/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import com.chuntung.payment.dto.*;

public interface PaymentVendor<T> {

    FormResult<?> preparePay(PayReq<T> req);

    PayQueryResult queryPay(PayQueryReq req);

    void closePay(PayQueryReq req);

    void refund(RefundReq req);

    RefundQueryResult queryRefund(RefundQueryReq req);

    Class<T> getParamType();
}