package com.chuntung.payment.dto;

public class RefundQueryReq extends BaseReq {
    private static final long serialVersionUID = -1487383097966280789L;

    private String refundRequestNo;

    public String getRefundRequestNo() {
        return refundRequestNo;
    }

    public void setRefundRequestNo(String refundRequestNo) {
        this.refundRequestNo = refundRequestNo;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RefundQueryReq{");
        sb.append("refundRequestNo='").append(refundRequestNo).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
