/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.controller;

import com.chuntung.payment.dto.*;
import com.chuntung.payment.service.PaymentBridge;
import com.chuntung.payment.service.PaymentCallbackFacade;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController("payment")
public class PaymentController {
    @Resource
    private PaymentCallbackFacade paymentCallbackFacade;

    @Resource
    private PaymentBridge paymentBridge;

    @PostMapping("prepare")
    public FormResult preparePay(@RequestBody PayReq<Object> req) {
        return paymentBridge.preparePay(req);
    }

    @PostMapping("query")
    public PayQueryResult queryPay(@RequestBody PayQueryReq req) {
        return paymentBridge.queryPay(req);
    }

    @PostMapping("close")
    public void closePay(@RequestBody PayQueryReq req) {
        paymentBridge.closePay(req);
    }

    @PostMapping("refund")
    public void refund(@RequestBody RefundReq req) {
        paymentBridge.refund(req);
    }

    @PostMapping("queryRefund")
    public RefundQueryResult queryRefund(@RequestBody RefundQueryReq req) {
        return paymentBridge.queryRefund(req);
    }

    /**
     * Generic async payment callback endpoint.
     * The {vendor} path variable must match a {@link com.chuntung.payment.dto.PaymentVendorEnum} name.
     * Supports both XML bodies (e.g. WXPay) and URL-encoded form bodies (e.g. AliPay).
     */
    @PostMapping("callback/{vendor}/pay")
    public String payCallback(@PathVariable String vendor, HttpServletRequest request) throws IOException {
        return paymentCallbackFacade.handlePayCallback(vendor, readBody(request));
    }

    /**
     * Generic async refund callback endpoint.
     */
    @PostMapping("callback/{vendor}/refund")
    public String refundCallback(@PathVariable String vendor, HttpServletRequest request) throws IOException {
        return paymentCallbackFacade.handleRefundCallback(vendor, readBody(request));
    }

    /**
     * Reads the raw request body as a string.
     * For URL-encoded form requests, reconstructs the body from parsed parameters
     * (the servlet container may have consumed the input stream while populating params).
     */
    private String readBody(HttpServletRequest request) throws IOException {
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                for (String value : entry.getValue()) {
                    if (sb.length() > 0) {
                        sb.append('&');
                    }
                    sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                      .append('=')
                      .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
                }
            }
            return sb.toString();
        }
        return StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
    }
}
