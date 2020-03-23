package com.chuntung.payment.dto;

import java.io.Serializable;

public class BaseReq implements Serializable {
    private static final long serialVersionUID = 8668114099091655948L;

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BaseReq{");
        sb.append("vendor=").append(vendor);
        sb.append(", from=").append(from);
        sb.append('}');
        return sb.toString();
    }
}
