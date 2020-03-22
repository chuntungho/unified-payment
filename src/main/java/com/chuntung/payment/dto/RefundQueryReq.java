package com.chuntung.payment.dto;

public class RefundQueryReq extends BaseReq {
    private String refundRequestNo;

    public String getRefundRequestNo() {
        return refundRequestNo;
    }

    public void setRefundRequestNo(String refundRequestNo) {
        this.refundRequestNo = refundRequestNo;
    }
}
