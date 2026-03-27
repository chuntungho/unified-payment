/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto.alipay;

import java.io.Serializable;

public class AliPayParam implements Serializable {
    private static final long serialVersionUID = 3872145690124578031L;

    // 小程序支付需要买家的支付宝用户ID
    private String buyerId;

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AliPayParam{");
        sb.append("buyerId='").append(buyerId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
