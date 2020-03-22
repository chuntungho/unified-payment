package com.chuntung.payment.dto;

import java.math.BigDecimal;
import java.util.Date;

public class PayQueryResult {
    private PayStateEnum state;
    // 是否有退款
    private Boolean refunded;

    // 第三方交易号
    private String tradeNo;
    // 支付完成时间
    private Date tradeTime;
    // 本地请求号
    private String requestNo;
    // 支付金额
    private BigDecimal amount;

    public PayStateEnum getState() {
        return state;
    }

    public void setState(PayStateEnum state) {
        this.state = state;
    }

    public Boolean getRefunded() {
        return refunded;
    }

    public void setRefunded(Boolean refunded) {
        this.refunded = refunded;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Date getTradeTime() {
        return tradeTime;
    }

    public void setTradeTime(Date tradeTime) {
        this.tradeTime = tradeTime;
    }

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
}
