/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class RefundResp implements Serializable {
    private static final long serialVersionUID = 6657364612503763189L;

    // 是否退款成功
    private boolean success;
    private String errorCode;
    private String errorMsg;

    private String requestNo;
    private BigDecimal amount;

    private String tradeNo;
    private Date tradeTime;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefundResp{");
        sb.append("success=").append(success);
        sb.append(", errorCode='").append(errorCode).append('\'');
        sb.append(", errorMsg='").append(errorMsg).append('\'');
        sb.append(", requestNo='").append(requestNo).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", tradeNo='").append(tradeNo).append('\'');
        sb.append(", tradeTime=").append(tradeTime);
        sb.append('}');
        return sb.toString();
    }
}
