/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service.impl.wxpay;

import com.github.wxpay.sdk.IWXPayDomain;
import com.github.wxpay.sdk.WXPayConfig;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.util.ResourceUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class WXPayConfigWrapper extends WXPayConfig {
    private String appID;
    private String mchID;
    private String key;
    private String cert;

    final class WeChatPayDomain implements IWXPayDomain {
        @Override
        public void report(String s, long l, Exception e) {
            // NOOP
        }

        @Override
        public DomainInfo getDomain(WXPayConfig wxPayConfig) {
            return new DomainInfo(WXPayConstants.DOMAIN_API, true);
        }
    }

    public WXPayConfigWrapper(String appID, String mchID, String key, String cert) {
        this.appID = appID;
        this.mchID = mchID;
        this.key = key;
        this.cert = cert;
    }

    @Override
    public String getAppID() {
        return appID;
    }

    @Override
    public String getMchID() {
        return mchID;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void resetKey(String key) {
        this.key = key;
    }

    public InputStream getCertStream() {
        InputStream inputStream = null;
        if (cert.startsWith("classpath:")) {
            try {
                inputStream = ResourceUtils.getURL(cert).openStream();
            } catch (IOException e) {
                // NOOP
            }
        } else {
            // Base64.getEncoder().encodeToString(bytes);
            byte[] bytes = Base64.getDecoder().decode(cert);
            inputStream = new ByteArrayInputStream(bytes);
        }
        return inputStream;
    }

    public IWXPayDomain getWXPayDomain() {
        return new WeChatPayDomain();
    }

}
