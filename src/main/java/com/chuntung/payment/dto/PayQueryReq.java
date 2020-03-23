package com.chuntung.payment.dto;

public class PayQueryReq extends BaseReq {
    private static final long serialVersionUID = 9166199270496119477L;

    private String payRequestNo;

    public String getPayRequestNo() {
        return payRequestNo;
    }

    public void setPayRequestNo(String payRequestNo) {
        this.payRequestNo = payRequestNo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PayQueryReq{");
        sb.append("payRequestNo='").append(payRequestNo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
