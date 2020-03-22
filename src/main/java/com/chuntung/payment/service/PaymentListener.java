package com.chuntung.payment.service;

import com.chuntung.payment.dto.PayResp;
import com.chuntung.payment.dto.RefundResp;

public interface PaymentListener {
    /**
     * 支付后通知，自行忽略非本方数据
     *
     * @param resp
     */
    void onPay(PayResp resp);

    /**
     * 退款后通知，自行忽略非本方数据
     *
     * @param resp
     */
    void onRefund(RefundResp resp);
}
