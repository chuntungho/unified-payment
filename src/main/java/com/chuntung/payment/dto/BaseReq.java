package com.chuntung.payment.dto;

public class BaseReq {
    private PaymentVendorEnum vendor;
    private PayFromEnum from;

    public PaymentVendorEnum getVendor() {
        return vendor;
    }

    public void setVendor(PaymentVendorEnum vendor) {
        this.vendor = vendor;
    }

    public PayFromEnum getFrom() {
        return from;
    }

    public void setFrom(PayFromEnum from) {
        this.from = from;
    }
}
