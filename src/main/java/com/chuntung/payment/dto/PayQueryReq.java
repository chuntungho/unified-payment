package com.chuntung.payment.dto;

public class PayQueryReq extends BaseReq {
    private String payRequestNo;

    public String getPayRequestNo() {
        return payRequestNo;
    }

    public void setPayRequestNo(String payRequestNo) {
        this.payRequestNo = payRequestNo;
    }
}
