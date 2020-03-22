package com.chuntung.payment.dto;

import java.math.BigDecimal;
import java.util.Date;

public class RefundQueryResult {
    private RefundStateEnum state;

    // 第三方交易号
    private String tradeNo;
    // 支付完成时间
    private Date tradeTime;
    // 本地请求号
    private String requestNo;
    // 退款金额
    private BigDecimal amount;

    public RefundStateEnum getState() {
        return state;
    }

    public void setState(RefundStateEnum state) {
        this.state = state;
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
