/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import com.chuntung.payment.dto.PaymentVendorEnum;

/**
 * Implemented by payment vendors that support async payment/refund callbacks.
 * Each implementing vendor self-registers via {@link PaymentBridge#registerCallbackVendor}.
 */
public interface CallbackVendor {

    PaymentVendorEnum vendorEnum();

    /**
     * Handle async payment notification from the payment provider.
     *
     * @param body raw HTTP request body (XML for WXPay, URL-encoded form for AliPay, etc.)
     * @return response string expected by the payment provider
     */
    String payCallback(String body);

    /**
     * Handle async refund notification from the payment provider.
     *
     * @param body raw HTTP request body
     * @return response string expected by the payment provider
     */
    String refundCallback(String body);
}
