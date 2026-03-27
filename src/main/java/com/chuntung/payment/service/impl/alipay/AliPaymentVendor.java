/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service.impl.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.chuntung.payment.conf.AliPaymentProperties;
import com.chuntung.payment.dto.*;
import com.chuntung.payment.dto.alipay.AliPayParam;
import com.chuntung.payment.service.CallbackVendor;
import com.chuntung.payment.service.PaymentBridge;
import com.chuntung.payment.service.PaymentException;
import com.chuntung.payment.service.PaymentVendor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class AliPaymentVendor implements PaymentVendor<AliPayParam>, CallbackVendor {
    private static final Logger logger = LoggerFactory.getLogger(AliPaymentVendor.class);
    private static final String CHARSET = "UTF-8";
    private static final String SIGN_TYPE = "RSA2";
    private static final String FORMAT = "json";
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Resource
    private AliPaymentProperties aliPaymentProperties;

    @Resource
    private PaymentBridge paymentBridge;

    @PostConstruct
    private void init() {
        paymentBridge.registerVendor(PaymentVendorEnum.AliPay, this);
        paymentBridge.registerCallbackVendor(this);
    }

    @Override
    public PaymentVendorEnum vendorEnum() {
        return PaymentVendorEnum.AliPay;
    }

    private AlipayClient getClient() {
        return new DefaultAlipayClient(
                aliPaymentProperties.getGatewayUrl(),
                aliPaymentProperties.getAppId(),
                aliPaymentProperties.getPrivateKey(),
                FORMAT, CHARSET,
                aliPaymentProperties.getAlipayPublicKey(),
                SIGN_TYPE);
    }

    @Override
    public FormResult<?> preparePay(PayReq<AliPayParam> req) {
        try {
            AlipayClient client = getClient();
            String outTradeNo = req.getRequestNo();
            String totalAmount = req.getAmount().toPlainString();
            String subject = req.getContent() != null ? req.getContent() : outTradeNo;
            AliPayParam param = req.getSpecialParam();
            String notifyUrl = aliPaymentProperties.getNotifyUrl();
            String returnUrl = aliPaymentProperties.getReturnUrl();

            switch (req.getFrom()) {
                case PC: {
                    AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
                    request.setNotifyUrl(notifyUrl);
                    request.setReturnUrl(returnUrl);
                    AlipayTradePagePayModel model = new AlipayTradePagePayModel();
                    model.setOutTradeNo(outTradeNo);
                    model.setTotalAmount(totalAmount);
                    model.setSubject(subject);
                    model.setProductCode("FAST_INSTANT_TRADE_PAY");
                    request.setBizModel(model);
                    return new FormResult<>(null, client.pageExecute(request).getBody());
                }
                case H5: {
                    AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
                    request.setNotifyUrl(notifyUrl);
                    request.setReturnUrl(returnUrl);
                    AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
                    model.setOutTradeNo(outTradeNo);
                    model.setTotalAmount(totalAmount);
                    model.setSubject(subject);
                    model.setProductCode("QUICK_WAP_WAY");
                    request.setBizModel(model);
                    return new FormResult<>(null, client.pageExecute(request).getBody());
                }
                case APP: {
                    AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
                    request.setNotifyUrl(notifyUrl);
                    AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
                    model.setOutTradeNo(outTradeNo);
                    model.setTotalAmount(totalAmount);
                    model.setSubject(subject);
                    model.setProductCode("QUICK_MSECURITY_PAY");
                    request.setBizModel(model);
                    return new FormResult<>(null, client.sdkExecute(request).getBody());
                }
                case EMBEDDED: {
                    // 小程序支付：创建交易后由客户端调起支付
                    AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
                    request.setNotifyUrl(notifyUrl);
                    AlipayTradeCreateModel model = new AlipayTradeCreateModel();
                    model.setOutTradeNo(outTradeNo);
                    model.setTotalAmount(totalAmount);
                    model.setSubject(subject);
                    if (param != null && param.getBuyerId() != null) {
                        model.setBuyerId(param.getBuyerId());
                    }
                    request.setBizModel(model);
                    AlipayTradeCreateResponse response = client.execute(request);
                    if (!response.isSuccess()) {
                        logger.error("Failed to create alipay trade: {} - {}", response.getCode(), response.getMsg());
                        throw new PaymentException("Failed to request payment");
                    }
                    return new FormResult<>(null, response.getTradeNo());
                }
                default:
                    throw new PaymentException("Unsupported pay from: " + req.getFrom());
            }
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to prepare alipay pay", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public PayQueryResult queryPay(PayQueryReq req) {
        try {
            AlipayClient client = getClient();
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(req.getPayRequestNo());
            request.setBizModel(model);
            AlipayTradeQueryResponse response = client.execute(request);

            if (!response.isSuccess()) {
                logger.error("Failed to query alipay trade: {} - {}", response.getCode(), response.getMsg());
                throw new PaymentException("Failed to query payment");
            }

            PayQueryResult result = new PayQueryResult();
            result.setRequestNo(response.getOutTradeNo());

            String tradeStatus = response.getTradeStatus();
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                result.setState(PayStateEnum.SUCCESS);
                String refundAmount = response.getRefundAmount();
                boolean refunded = refundAmount != null && new BigDecimal(refundAmount).compareTo(BigDecimal.ZERO) > 0;
                result.setRefunded(refunded);
                if (!refunded) {
                    result.setTradeNo(response.getTradeNo());
                    result.setTradeTime(parseDate(response.getSendPayDate()));
                    String buyerPayAmount = response.getBuyerPayAmount();
                    if (buyerPayAmount != null) {
                        result.setAmount(new BigDecimal(buyerPayAmount));
                    }
                }
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                result.setState(PayStateEnum.CANCELED);
            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                result.setState(PayStateEnum.PAYING);
            } else {
                result.setState(PayStateEnum.INIT);
            }

            return result;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to query alipay pay", e);
            throw new PaymentException("Failed to query payment");
        }
    }

    @Override
    public void closePay(PayQueryReq req) {
        try {
            AlipayClient client = getClient();
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(req.getPayRequestNo());
            request.setBizModel(model);
            AlipayTradeCloseResponse response = client.execute(request);
            if (!response.isSuccess()) {
                logger.error("Failed to close alipay trade: {} - {}", response.getCode(), response.getMsg());
                throw new PaymentException("Failed to close payment");
            }
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to close alipay pay", e);
            throw new PaymentException("Failed to close payment");
        }
    }

    @Override
    public void refund(RefundReq req) {
        try {
            AlipayClient client = getClient();
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(req.getPayRequestNo());
            model.setRefundAmount(req.getAmount().toPlainString());
            model.setOutRequestNo(req.getRequestNo());
            if (req.getContent() != null) {
                model.setRefundReason(req.getContent());
            }
            request.setBizModel(model);
            AlipayTradeRefundResponse response = client.execute(request);
            if (!response.isSuccess()) {
                logger.error("Failed to refund alipay trade: {} - {}", response.getCode(), response.getMsg());
                throw new PaymentException("Failed to request refund");
            }
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to refund alipay pay", e);
            throw new PaymentException("Failed to request refund");
        }
    }

    @Override
    public RefundQueryResult queryRefund(RefundQueryReq req) {
        try {
            AlipayClient client = getClient();
            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
            model.setOutRequestNo(req.getRefundRequestNo());
            request.setBizModel(model);
            AlipayTradeFastpayRefundQueryResponse response = client.execute(request);

            RefundQueryResult result = new RefundQueryResult();
            result.setRequestNo(response.getOutRequestNo());

            if (!response.isSuccess()) {
                logger.error("Failed to query alipay refund: {} - {}", response.getCode(), response.getMsg());
                result.setState(RefundStateEnum.IN_PROCESS);
                return result;
            }

            String refundStatus = response.getRefundStatus();
            if ("REFUND_SUCCESS".equals(refundStatus)) {
                result.setState(RefundStateEnum.SUCCESS);
                result.setTradeNo(response.getTradeNo());
                String refundAmount = response.getRefundAmount();
                if (refundAmount != null) {
                    result.setAmount(new BigDecimal(refundAmount));
                }
                result.setTradeTime(parseDate(response.getGmtRefundPay()));
            } else if ("REFUND_FAIL".equals(refundStatus)) {
                result.setState(RefundStateEnum.CANCELLED);
            } else {
                result.setState(RefundStateEnum.IN_PROCESS);
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to query alipay refund", e);
            throw new PaymentException("Failed to query refund");
        }
    }

    /**
     * Handles AliPay async payment notification.
     * Accepts a URL-encoded form body as sent by AliPay servers.
     */
    @Override
    public String payCallback(String body) {
        try {
            Map<String, String> params = parseFormBody(body);
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, aliPaymentProperties.getAlipayPublicKey(), CHARSET, SIGN_TYPE);
            if (!signVerified) {
                logger.error("AliPay payment callback signature verification failed");
                return "failure";
            }

            String tradeStatus = params.get("trade_status");
            PayResp resp = new PayResp();
            resp.setSuccess("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus));
            resp.setRequestNo(params.get("out_trade_no"));
            if (resp.isSuccess()) {
                resp.setTradeNo(params.get("trade_no"));
                String receiptAmount = params.get("receipt_amount");
                if (receiptAmount != null) {
                    resp.setAmount(new BigDecimal(receiptAmount));
                }
                resp.setTradeTime(parseDate(params.get("gmt_payment")));
            } else {
                resp.setErrorCode(tradeStatus);
            }

            paymentBridge.notifyPayResult(resp);
            return "success";
        } catch (Exception e) {
            logger.error("Failed to process alipay payment callback", e);
            return "failure";
        }
    }

    /**
     * Handles AliPay async refund notification.
     * Accepts a URL-encoded form body as sent by AliPay servers.
     */
    @Override
    public String refundCallback(String body) {
        try {
            Map<String, String> params = parseFormBody(body);
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params, aliPaymentProperties.getAlipayPublicKey(), CHARSET, SIGN_TYPE);
            if (!signVerified) {
                logger.error("AliPay refund callback signature verification failed");
                return "failure";
            }

            RefundResp resp = new RefundResp();
            resp.setSuccess(true);
            resp.setRequestNo(params.get("out_biz_no"));
            resp.setTradeNo(params.get("trade_no"));
            String refundFee = params.get("refund_fee");
            if (refundFee != null) {
                resp.setAmount(new BigDecimal(refundFee));
            }
            resp.setTradeTime(parseDate(params.get("gmt_refund")));

            paymentBridge.notifyRefundResult(resp);
            return "success";
        } catch (Exception e) {
            logger.error("Failed to process alipay refund callback", e);
            return "failure";
        }
    }

    @Override
    public Class<AliPayParam> getParamType() {
        return AliPayParam.class;
    }

    /**
     * Parses a URL-encoded form body into a key-value map.
     * Values are URL-decoded, matching what a servlet container would provide via getParameterMap().
     */
    private Map<String, String> parseFormBody(String body) throws Exception {
        Map<String, String> params = new HashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), CHARSET);
                String value = idx < pair.length() - 1
                        ? URLDecoder.decode(pair.substring(idx + 1), CHARSET)
                        : "";
                params.put(key, value);
            }
        }
        return params;
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
