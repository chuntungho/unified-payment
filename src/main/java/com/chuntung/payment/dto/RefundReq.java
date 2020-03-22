package com.chuntung.payment.dto;

import java.math.BigDecimal;

public class RefundReq extends BaseReq {
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
}
