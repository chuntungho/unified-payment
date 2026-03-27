/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.service.impl.unionpay;

import com.chuntung.payment.conf.UnionPaymentProperties;
import com.chuntung.payment.dto.*;
import com.chuntung.payment.service.CallbackVendor;
import com.chuntung.payment.service.PaymentBridge;
import com.chuntung.payment.service.PaymentException;
import com.chuntung.payment.service.PaymentVendor;
import com.egzosn.pay.common.bean.CertStoreType;
import com.egzosn.pay.common.bean.NoticeParams;
import com.egzosn.pay.common.bean.PayOrder;
import com.egzosn.pay.common.bean.RefundOrder;
import com.egzosn.pay.union.api.UnionPayConfigStorage;
import com.egzosn.pay.union.api.UnionPayService;
import com.egzosn.pay.union.bean.SDKConstants;
import com.egzosn.pay.union.bean.UnionRefundResult;
import com.egzosn.pay.union.bean.UnionTransactionType;
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
public class UnionPaymentVendor implements PaymentVendor<Void>, CallbackVendor {
    private static final Logger logger = LoggerFactory.getLogger(UnionPaymentVendor.class);
    private static final String CHARSET = "UTF-8";
    // UnionPay txnAmt in response is in fen (cents)
    private static final BigDecimal YUAN_TO_FEN = new BigDecimal("100");
    // txnTime format: yyyyMMddHHmmss
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";

    @Resource
    private UnionPaymentProperties unionPaymentProperties;

    @Resource
    private PaymentBridge paymentBridge;

    private volatile UnionPayService service;

    @PostConstruct
    private void init() {
        paymentBridge.registerVendor(PaymentVendorEnum.UnionPay, this);
        paymentBridge.registerCallbackVendor(this);
    }

    @Override
    public PaymentVendorEnum vendorEnum() {
        return PaymentVendorEnum.UnionPay;
    }

    private UnionPayService getService() {
        if (service == null) {
            synchronized (this) {
                if (service == null) {
                    UnionPayConfigStorage config = new UnionPayConfigStorage();
                    config.setMerId(unionPaymentProperties.getMerId());
                    config.setKeyPrivateCert(unionPaymentProperties.getPrivateKeyPath());
                    config.setKeyPrivateCertPwd(unionPaymentProperties.getPrivateKeyPwd());
                    config.setAcpMiddleCert(unionPaymentProperties.getAcpMiddleCertPath());
                    config.setAcpRootCert(unionPaymentProperties.getAcpRootCertPath());
                    config.setCertStoreType(CertStoreType.PATH);
                    config.setTest(unionPaymentProperties.isSandbox());
                    config.setNotifyUrl(unionPaymentProperties.getNotifyUrl());
                    config.setReturnUrl(unionPaymentProperties.getReturnUrl());
                    config.setInputCharset(CHARSET);
                    service = new UnionPayService(config);
                }
            }
        }
        return service;
    }

    @Override
    public FormResult<?> preparePay(PayReq<Void> req) {
        try {
            UnionPayService svc = getService();
            PayOrder order = new PayOrder();
            order.setOutTradeNo(req.getRequestNo());
            // price in yuan; SDK converts to fen internally via Util.conversionCentAmount()
            order.setPrice(req.getAmount());

            switch (req.getFrom()) {
                case PC:
                case H5: {
                    // toPay() sets WEB (channelType=07); suitable for both PC and mobile browser
                    String form = svc.toPay(order);
                    return new FormResult<>(null, form);
                }
                case APP: {
                    // app() returns a map containing the tn token
                    Map<String, Object> params = svc.app(order);
                    String tn = (String) params.get(SDKConstants.param_tn);
                    if (tn == null) {
                        logger.error("UnionPay APP pay returned no tn token");
                        throw new PaymentException("Failed to request payment");
                    }
                    return new FormResult<>(null, tn);
                }
                case EMBEDDED: {
                    // Scan-code QR pay (merchant presents QR code for customer to scan)
                    order.setTransactionType(UnionTransactionType.APPLY_QR_CODE);
                    String qrCode = svc.getQrPay(order);
                    return new FormResult<>(null, qrCode);
                }
                default:
                    throw new PaymentException("Unsupported pay from: " + req.getFrom());
            }
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to prepare UnionPay pay", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public PayQueryResult queryPay(PayQueryReq req) {
        try {
            UnionPayService svc = getService();
            Map<String, Object> resp = svc.query(null, req.getPayRequestNo());

            PayQueryResult result = new PayQueryResult();
            result.setRequestNo(req.getPayRequestNo());

            String respCode = (String) resp.get(SDKConstants.param_respCode);
            if (SDKConstants.OK_RESP_CODE.equals(respCode)) {
                result.setState(PayStateEnum.SUCCESS);
                result.setTradeNo((String) resp.get(SDKConstants.param_queryId));
                String txnAmt = (String) resp.get(SDKConstants.param_txnAmt);
                if (txnAmt != null && !txnAmt.isEmpty()) {
                    result.setAmount(new BigDecimal(txnAmt).divide(YUAN_TO_FEN));
                }
                result.setTradeTime(parseDate((String) resp.get(SDKConstants.param_txnTime)));
            } else if ("02".equals(respCode)) {
                // "02" = order being processed
                result.setState(PayStateEnum.PAYING);
            } else {
                result.setState(PayStateEnum.FAILED);
            }
            return result;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to query UnionPay pay", e);
            throw new PaymentException("Failed to query payment");
        }
    }

    @Override
    public void closePay(PayQueryReq req) {
        // UnionPay does not support trade cancellation via API
        throw new PaymentException("UnionPay does not support closePay");
    }

    @Override
    public void refund(RefundReq req) {
        try {
            UnionPayService svc = getService();
            // origQryId = the queryId from the original payment (stored as payRequestNo by callers)
            RefundOrder order = new RefundOrder(req.getRequestNo(), req.getPayRequestNo(), req.getAmount());
            UnionRefundResult result = svc.refund(order);
            if (!SDKConstants.OK_RESP_CODE.equals(result.getRespCode())) {
                logger.error("Failed to refund UnionPay trade: {}", result.getRespCode());
                throw new PaymentException("Failed to request refund");
            }
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to refund UnionPay pay", e);
            throw new PaymentException("Failed to request refund");
        }
    }

    @Override
    public RefundQueryResult queryRefund(RefundQueryReq req) {
        try {
            UnionPayService svc = getService();
            Map<String, Object> resp = svc.query(null, req.getRefundRequestNo());

            RefundQueryResult result = new RefundQueryResult();
            result.setRequestNo(req.getRefundRequestNo());

            String origRespCode = (String) resp.get(SDKConstants.param_origRespCode);
            if (SDKConstants.OK_RESP_CODE.equals(origRespCode)) {
                result.setState(RefundStateEnum.SUCCESS);
                result.setTradeNo((String) resp.get(SDKConstants.param_queryId));
                String txnAmt = (String) resp.get(SDKConstants.param_txnAmt);
                if (txnAmt != null && !txnAmt.isEmpty()) {
                    result.setAmount(new BigDecimal(txnAmt).divide(YUAN_TO_FEN));
                }
                result.setTradeTime(parseDate((String) resp.get(SDKConstants.param_txnTime)));
            } else if (origRespCode == null || origRespCode.isEmpty()) {
                result.setState(RefundStateEnum.IN_PROCESS);
            } else {
                result.setState(RefundStateEnum.CANCELLED);
            }
            return result;
        } catch (Exception e) {
            logger.error("Failed to query UnionPay refund", e);
            throw new PaymentException("Failed to query refund");
        }
    }

    /**
     * Handles UnionPay async payment notification.
     * Accepts a URL-encoded form body as sent by UnionPay servers.
     */
    @Override
    public String payCallback(String body) {
        try {
            Map<String, Object> params = parseFormBody(body);
            boolean signVerified = getService().verify(new NoticeParams(params));
            if (!signVerified) {
                logger.error("UnionPay payment callback signature verification failed");
                return "failure";
            }

            String respCode = (String) params.get(SDKConstants.param_respCode);
            PayResp resp = new PayResp();
            resp.setSuccess(SDKConstants.OK_RESP_CODE.equals(respCode));
            resp.setRequestNo((String) params.get(SDKConstants.param_orderId));
            if (resp.isSuccess()) {
                resp.setTradeNo((String) params.get(SDKConstants.param_queryId));
                String txnAmt = (String) params.get(SDKConstants.param_txnAmt);
                if (txnAmt != null) {
                    resp.setAmount(new BigDecimal(txnAmt).divide(YUAN_TO_FEN));
                }
                resp.setTradeTime(parseDate((String) params.get(SDKConstants.param_txnTime)));
            } else {
                resp.setErrorCode(respCode);
            }

            paymentBridge.notifyPayResult(resp);
            return "success";
        } catch (Exception e) {
            logger.error("Failed to process UnionPay payment callback", e);
            return "failure";
        }
    }

    /**
     * Handles UnionPay async refund notification.
     * Accepts a URL-encoded form body as sent by UnionPay servers.
     */
    @Override
    public String refundCallback(String body) {
        try {
            Map<String, Object> params = parseFormBody(body);
            boolean signVerified = getService().verify(new NoticeParams(params));
            if (!signVerified) {
                logger.error("UnionPay refund callback signature verification failed");
                return "failure";
            }

            String origRespCode = (String) params.get(SDKConstants.param_origRespCode);
            RefundResp resp = new RefundResp();
            resp.setSuccess(SDKConstants.OK_RESP_CODE.equals(origRespCode));
            resp.setRequestNo((String) params.get(SDKConstants.param_orderId));
            if (resp.isSuccess()) {
                resp.setTradeNo((String) params.get(SDKConstants.param_queryId));
                String txnAmt = (String) params.get(SDKConstants.param_txnAmt);
                if (txnAmt != null) {
                    resp.setAmount(new BigDecimal(txnAmt).divide(YUAN_TO_FEN));
                }
                resp.setTradeTime(parseDate((String) params.get(SDKConstants.param_txnTime)));
            } else {
                resp.setErrorCode(origRespCode);
            }

            paymentBridge.notifyRefundResult(resp);
            return "success";
        } catch (Exception e) {
            logger.error("Failed to process UnionPay refund callback", e);
            return "failure";
        }
    }

    @Override
    public Class<Void> getParamType() {
        return Void.class;
    }

    /**
     * Parses a URL-encoded form body into a key-value map with Object values,
     * as required by {@link NoticeParams}.
     */
    private Map<String, Object> parseFormBody(String body) throws Exception {
        Map<String, Object> params = new HashMap<>();
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
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
