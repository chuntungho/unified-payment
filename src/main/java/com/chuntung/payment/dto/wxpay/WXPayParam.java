/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto.wxpay;

import java.io.Serializable;

public class WXPayParam implements Serializable {
    private static final long serialVersionUID = -689381524741278093L;

    // JSAPI 支付需要
    private String openId;

    // NATIVE 扫描支付需要
    private String productId;

    // 用于区分trade_type参数，微信浏览器则为JSAPI，其他浏览器为NWEB
    private Boolean wxBrowser;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Boolean getWxBrowser() {
        return wxBrowser;
    }

    public void setWxBrowser(Boolean wxBrowser) {
        this.wxBrowser = wxBrowser;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WXPayParam{");
        sb.append("openId='").append(openId).append('\'');
        sb.append(", productId='").append(productId).append('\'');
        sb.append(", wxBrowser=").append(wxBrowser);
        sb.append('}');
        return sb.toString();
    }
}
