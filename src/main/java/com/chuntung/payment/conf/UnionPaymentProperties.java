/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("payment.unionpay")
public class UnionPaymentProperties {
    // 商户号
    private String merId;
    // 商户私钥证书路径
    private String privateKeyPath;
    // 商户私钥证书密码
    private String privateKeyPwd;
    // 中级证书路径（acp_test_middle.cer / acp_middle.cer）
    private String acpMiddleCertPath;
    // 根证书路径（acp_test_root.cer / acp_root.cer）
    private String acpRootCertPath;
    // 支付结果通知地址
    private String notifyUrl;
    // 同步跳转地址（PC/H5支付）
    private String returnUrl;
    // 是否沙箱环境
    private boolean sandbox;

    public String getMerId() { return merId; }
    public void setMerId(String merId) { this.merId = merId; }

    public String getPrivateKeyPath() { return privateKeyPath; }
    public void setPrivateKeyPath(String privateKeyPath) { this.privateKeyPath = privateKeyPath; }

    public String getPrivateKeyPwd() { return privateKeyPwd; }
    public void setPrivateKeyPwd(String privateKeyPwd) { this.privateKeyPwd = privateKeyPwd; }

    public String getAcpMiddleCertPath() { return acpMiddleCertPath; }
    public void setAcpMiddleCertPath(String acpMiddleCertPath) { this.acpMiddleCertPath = acpMiddleCertPath; }

    public String getAcpRootCertPath() { return acpRootCertPath; }
    public void setAcpRootCertPath(String acpRootCertPath) { this.acpRootCertPath = acpRootCertPath; }

    public String getNotifyUrl() { return notifyUrl; }
    public void setNotifyUrl(String notifyUrl) { this.notifyUrl = notifyUrl; }

    public String getReturnUrl() { return returnUrl; }
    public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }

    public boolean isSandbox() { return sandbox; }
    public void setSandbox(boolean sandbox) { this.sandbox = sandbox; }
}
