package com.chuntung.payment.dto;

import java.math.BigDecimal;
import java.util.Map;

public class PayReq<T> extends BaseReq {
    // 请求号
    private String requestNo;
    // 支付内容
    private String content;
    // 支付金额
    private BigDecimal amount;

    // 不同支付特定的参数
    private T specialParam;

    private String clientIp;

    public String getRequestNo() {
        return requestNo;
    }

    public void setRequestNo(String requestNo) {
        this.requestNo = requestNo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public T getSpecialParam() {
        return specialParam;
    }

    public void setSpecialParam(T specialParam) {
        this.specialParam = specialParam;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
