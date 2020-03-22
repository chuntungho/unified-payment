package com.chuntung.payment.dto.wxpay;

public class WXPayParam {
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
}
