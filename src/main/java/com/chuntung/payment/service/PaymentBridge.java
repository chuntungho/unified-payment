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
    private List<PaymentListener> listeners = new CopyOnWriteArrayList<>();

    @Resource
    private ObjectMapper objectMapper;

    private PaymentVendor getPayVendor(PaymentVendorEnum vendor) {
        return vendors.get(vendor);
    }

    /**
     * 增加支持的支付渠道
     *
     * @param vendor
     * @param vendorImpl
     */
    public void registerVendor(PaymentVendorEnum vendor, PaymentVendor vendorImpl) {
        vendors.put(vendor, vendorImpl);
    }

    /**
     * 注册支付结果监听
     *
     * @param listener
     */
    public void registerListener(PaymentListener listener) {
        listeners.add(listener);
    }

    /**
     * 准备支付参数
     *
     * @param req
     * @return
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
     *
     * @param req
     * @return
     */
    public PayQueryResult queryPay(PayQueryReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        return paymentVendor.queryPay(req);
    }

    /**
     * 撤销支付请求
     *
     * @param req
     */
    public void closePay(PayQueryReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        paymentVendor.closePay(req);
    }

    /**
     * 退款
     *
     * @param req
     */
    public void refund(RefundReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        paymentVendor.refund(req);
    }

    /**
     * 查询退款结果
     *
     * @param req
     * @return
     */
    public RefundQueryResult queryRefund(RefundQueryReq req) {
        PaymentVendor paymentVendor = getPayVendor(req.getVendor());
        return paymentVendor.queryRefund(req);
    }

    /**
     * 支付结果通知
     *
     * @param resp
     */
    public void notifyPayResult(PayResp resp) {
        for (PaymentListener listener : listeners) {
            listener.onPay(resp);
        }
    }

    /**
     * 退款结果通知
     *
     * @param resp
     */
    public void notifyRefundResult(RefundResp resp) {
        for (PaymentListener listener : listeners) {
            listener.onRefund(resp);
        }
    }
}