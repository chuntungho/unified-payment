/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class PayResp implements Serializable {
    private static final long serialVersionUID = -4933413366158026192L;

    // 是否支付成功
    private boolean success;
    private String errorCode;
    private String errorMsg;

    // 第三方交易号
    private String tradeNo;
    // 支付完成时间
    private Date tradeTime;
    // 本地请求号
    private String requestNo;
    // 支付金额
    private BigDecimal amount;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayResp{");
        sb.append("success=").append(success);
        sb.append(", errorCode='").append(errorCode).append('\'');
        sb.append(", errorMsg='").append(errorMsg).append('\'');
        sb.append(", tradeNo='").append(tradeNo).append('\'');
        sb.append(", tradeTime=").append(tradeTime);
        sb.append(", requestNo='").append(requestNo).append('\'');
        sb.append(", amount=").append(amount);
        sb.append('}');
        return sb.toString();
    }
}
