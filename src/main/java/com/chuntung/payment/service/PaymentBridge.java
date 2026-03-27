/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service;

import com.chuntung.payment.dto.*;
import com.chuntung.payment.dto.wxpay.WXPayParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Unified Payment Bridge
 */
@Component
public class PaymentBridge {
    private Map<PaymentVendorEnum, PaymentVendor> vendors = new ConcurrentHashMap<>();
    private Map<PaymentVendorEnum, CallbackVendor> callbackVendors = new ConcurrentHashMap<>();
    private List<PaymentListener> listeners = new CopyOnWriteArrayList<>();

    @Resource
    private ObjectMapper objectMapper;

    private PaymentVendor getPayVendor(PaymentVendorEnum vendor) {
        return vendors.get(vendor);
    }

    /**
     * 增加支持的支付渠道
     */
    public void registerVendor(PaymentVendorEnum vendor, PaymentVendor vendorImpl) {
        vendors.put(vendor, vendorImpl);
    }

    /**
     * 注册支付回调处理器
     */
    public void registerCallbackVendor(CallbackVendor handler) {
        callbackVendors.put(handler.vendorEnum(), handler);
    }

    /**
     * 注册支付结果监听
     */
    public void registerListener(PaymentListener listener) {
        listeners.add(listener);
    }

    /**
     * 准备支付参数
     */
    public FormResult preparePay(PayReq<Object> req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        if (req.getSpecialParam() != null) {
            Object param = objectMapper.convertValue(req.getSpecialParam(), paymentVendor.getParamType());
            req.setSpecialParam(param);
        }
        return paymentVendor.preparePay(req);
    }

    /**
     * 查询支付结果
     */
    public PayQueryResult queryPay(PayQueryReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        return paymentVendor.queryPay(req);
    }

    /**
     * 撤销支付请求
     */
    public void closePay(PayQueryReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        paymentVendor.closePay(req);
    }

    /**
     * 退款
     */
    public void refund(RefundReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        paymentVendor.refund(req);
    }

    /**
     * 查询退款结果
     */
    public RefundQueryResult queryRefund(RefundQueryReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        return paymentVendor.queryRefund(req);
    }

    /**
     * 路由支付回调
     *
     * @param vendorName {@link PaymentVendorEnum} name
     * @param body       raw HTTP request body
     */
    public String handlePayCallback(String vendorName, String body) {
        CallbackVendor handler = resolveCallbackVendor(vendorName);
        return handler.payCallback(body);
    }

    /**
     * 路由退款回调
     *
     * @param vendorName {@link PaymentVendorEnum} name
     * @param body       raw HTTP request body
     */
    public String handleRefundCallback(String vendorName, String body) {
        CallbackVendor handler = resolveCallbackVendor(vendorName);
        return handler.refundCallback(body);
    }

    private CallbackVendor resolveCallbackVendor(String vendorName) {
        PaymentVendorEnum vendorEnum;
        try {
            vendorEnum = PaymentVendorEnum.valueOf(vendorName);
        } catch (IllegalArgumentException e) {
            throw new PaymentException("Unknown payment vendor: " + vendorName);
        }
        CallbackVendor handler = callbackVendors.get(vendorEnum);
        if (handler == null) {
            throw new PaymentException("No callback handler registered for vendor: " + vendorName);
        }
        return handler;
    }

    /**
     * 支付结果通知
     */
    public void notifyPayResult(PayResp resp) {
        for (PaymentListener listener : listeners) {
            listener.onPay(resp);
        }
    }

    /**
     * 退款结果通知
     */
    public void notifyRefundResult(RefundResp resp) {
        for (PaymentListener listener : listeners) {
            listener.onRefund(resp);
        }
    }
}
