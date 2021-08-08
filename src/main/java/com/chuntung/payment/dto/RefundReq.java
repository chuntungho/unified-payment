/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto;

import java.math.BigDecimal;

public class RefundReq extends BaseReq {
    private static final long serialVersionUID = -6987556730271012424L;

    // 退款请求号
    private String requestNo;
    // 退款金额，支持部分退款
    private BigDecimal amount;
    // 退款原因
    private String content;
    // 支付请求号
    private String payRequestNo;
    // 微信退款需要原来金额！！！
    private BigDecimal originAmount;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPayRequestNo() {
        return payRequestNo;
    }

    public void setPayRequestNo(String payRequestNo) {
        this.payRequestNo = payRequestNo;
    }

    public BigDecimal getOriginAmount() {
        return originAmount;
    }

    public void setOriginAmount(BigDecimal originAmount) {
        this.originAmount = originAmount;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefundReq{");
        sb.append("requestNo='").append(requestNo).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", content='").append(content).append('\'');
        sb.append(", payRequestNo='").append(payRequestNo).append('\'');
        sb.append(", originAmount=").append(originAmount);
        sb.append('}');
        return sb.toString();
    }
}
