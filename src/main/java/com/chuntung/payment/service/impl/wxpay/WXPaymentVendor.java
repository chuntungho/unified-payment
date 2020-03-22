package com.chuntung.payment.service.impl.wxpay;

import com.chuntung.payment.service.PaymentBridge;
import com.chuntung.payment.service.PaymentException;
import com.chuntung.payment.service.PaymentVendor;
import com.chuntung.payment.conf.WXPaymentConfig;
import com.chuntung.payment.dto.*;
import com.chuntung.payment.dto.wxpay.WXPayPrepareResult;
import com.chuntung.payment.dto.wxpay.WXPayParam;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayConstants.SignType;
import com.github.wxpay.sdk.WXPayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class WXPaymentVendor implements PaymentVendor {
    private final static Logger logger = LoggerFactory.getLogger(WXPaymentVendor.class);
    private final static String RETURN_CODE = "return_code";
    private final static String RETURN_MSG = "return_msg";
    private final static String RESULT_CODE = "result_code";
    private final static String ERR_CODE = "err_code";
    private final static String ERR_CODE_DES = "err_code_des";

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private Map<PayFromEnum, String> tradeTypes = new HashMap<>();

    @Resource
    private WXPaymentConfig wxPaymentConfig;

    @Resource
    private PaymentBridge paymentBridge;

    @PostConstruct
    private void init() {
        tradeTypes.put(PayFromEnum.PC, "NATIVE");
        tradeTypes.put(PayFromEnum.MOBILE, "MWEB");
        tradeTypes.put(PayFromEnum.APP, "APP");
        tradeTypes.put(PayFromEnum.IN_APP, "JSAPI");

        paymentBridge.registerVendor(PaymentVendorEnum.WXPay, this);
    }

    private WXPay getClient(PayFromEnum from) throws Exception {
        String appId = wxPaymentConfig.getCorpId();
        if (PayFromEnum.APP.equals(from)) {
            appId = wxPaymentConfig.getOpenAppId();
        } else if (PayFromEnum.IN_APP.equals(from)) {
            appId = wxPaymentConfig.getWxAppId();
        }

        WXPayConfigWrapper config = new WXPayConfigWrapper(appId,
                wxPaymentConfig.getMchId(), wxPaymentConfig.getKey(), wxPaymentConfig.getCert());
        WXPay wxPay;
        if (Boolean.TRUE.equals(wxPaymentConfig.getSandbox())) {
            resetSandboxKey(config);
            wxPay = new WXPay(config, false, true);
        } else {
            wxPay = new WXPay(config);
        }

        return wxPay;
    }

    private void resetSandboxKey(WXPayConfigWrapper config) throws Exception {
        WXPay wxPay = new WXPay(config, false);
        Map<String, String> params = new HashMap<String, String>();
        params.put("mch_id", config.getMchID());
        params.put("nonce_str", WXPayUtil.generateNonceStr());
        params.put("sign", WXPayUtil.generateSignature(params, config.getKey()));
        String strXML = wxPay.requestWithoutCert("/sandboxnew/pay/getsignkey",
                params, config.getHttpConnectTimeoutMs(), config.getHttpReadTimeoutMs());

        Map<String, String> result = WXPayUtil.xmlToMap(strXML);
        if (WXPayConstants.SUCCESS.equals(result.get(RETURN_CODE))) {
            String signkey = result.get("sandbox_signkey");
            config.resetKey(signkey);
        }
    }

    /**
     * {@link com.github.wxpay.sdk.WXPay#WXPay(com.github.wxpay.sdk.WXPayConfig, java.lang.String, boolean, boolean)}
     *
     * @return
     */
    private SignType getSignType() {
        if (Boolean.TRUE.equals(wxPaymentConfig.getSandbox())) {
            return WXPayConstants.SignType.MD5;
        } else {
            return WXPayConstants.SignType.HMACSHA256;
        }
    }

    private void checkResponse(Map<String, String> resp, String action) {
        if (!WXPayConstants.SUCCESS.equals(resp.get(RETURN_CODE))) {
            String code = resp.containsKey("retcode") ? resp.get("retcode") : resp.get(RETURN_CODE);
            String msg = resp.containsKey("retmsg") ? resp.get("retmsg") : resp.get(RETURN_MSG);
            logger.error("Failed to {}: {} - {}", action, code, msg);
            throw new PaymentException("Failed to request payment");
        }

        if (!WXPayConstants.SUCCESS.equals(resp.get(RESULT_CODE))) {
            logger.error("Failed to {}: {} - {}", action, resp.get(ERR_CODE), resp.get(ERR_CODE_DES));
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public FormResult<WXPayPrepareResult> preparePay(PayReq req) {
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no", req.getRequestNo());
        if (req.getContent() != null) {
            data.put("body", req.getContent().length() > 128 ? req.getContent().substring(0, 128) : req.getContent());
        }
        // 单位分
        data.put("total_fee", String.valueOf(req.getAmount().multiply(HUNDRED).intValue()));
        data.put("spbill_create_ip", req.getClientIp());
        data.put("notify_url", wxPaymentConfig.getNotifyUrl());
        data.put("trade_type", tradeTypes.get(req.getFrom()));
        WXPayParam specialParam = (WXPayParam) req.getSpecialParam();
        if (specialParam != null) {
            if (specialParam.getOpenId() != null) {
                data.put("openid", specialParam.getOpenId().trim());
            }
            if (specialParam.getProductId() != null) {
                data.put("product_id", specialParam.getProductId().trim());
            }
            if (PayFromEnum.MOBILE.equals(req.getFrom())
                    && Boolean.TRUE.equals(specialParam.getWxBrowser())) {
                data.put("trade_type", "JSAPI");
            }
        }

        try {
            WXPay wxPay = getClient(req.getFrom());
            Map<String, String> resp = wxPay.unifiedOrder(data);
            checkResponse(resp, "prepare pay");

            // code_url, 用户进行二维码扫描
            if (!StringUtils.isEmpty(resp.get("code_url"))) {
                return new FormResult<>(resp.get("code_url"), null);
            }

            WXPayPrepareResult param = generateParam(resp, req.getFrom());
            return new FormResult<WXPayPrepareResult>(null, param);
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to prepare pay", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public PayQueryResult queryPay(PayQueryReq req) {
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no", req.getPayRequestNo());
        try {
            WXPay wxPay = getClient(req.getFrom());
            Map<String, String> resp = wxPay.orderQuery(data);
            checkResponse(resp, "query pay");

            PayQueryResult result = new PayQueryResult();
            result.setRequestNo(resp.get("out_trade_no"));

            String tradeState = resp.get("trade_state");
            if ("SUCCESS".equals(tradeState) || "REFUND".equals(tradeState)) {
                result.setState(PayStateEnum.SUCCESS);
                result.setRefunded("REFUND".equals(tradeState));
                if (!Boolean.TRUE.equals(result.getRefunded())) {
                    result.setTradeNo(resp.get("transaction_id"));
                    result.setTradeTime(WXPaymentUtil.parseDateForPay(resp.get("time_end")));
                    BigDecimal amount = new BigDecimal(resp.get("cash_fee")).divide(HUNDRED);
                    result.setAmount(amount);
                }
            } else if ("CLOSED".equals(tradeState) || "REVOKED".equals(tradeState)) {
                result.setState(PayStateEnum.CANCELED);
            } else if ("PAYERROR".equals(tradeState)) {
                result.setState(PayStateEnum.FAILED);
            } else if ("USERPAYING".equals(tradeState)) {
                result.setState(PayStateEnum.PAYING);
            } else {
                result.setState(PayStateEnum.INIT);
            }

            return result;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to query pay", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public void closePay(PayQueryReq req) {
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no", req.getPayRequestNo());
        try {
            WXPay wxPay = getClient(req.getFrom());
            Map<String, String> resp = wxPay.closeOrder(data);
            checkResponse(resp, "close pay");
        } catch (Exception e) {
            logger.error("Failed to close pay", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public void refund(RefundReq req) {
        Map<String, String> data = new HashMap<>();
        data.put("out_refund_no", req.getRequestNo());
        data.put("out_trade_no", req.getPayRequestNo());
        if (req.getContent() != null) {
            data.put("refund_desc", req.getContent().length() > 80 ? req.getContent().substring(0, 80) : req.getContent());
        }
        data.put("refund_fee", String.valueOf(req.getAmount().multiply(HUNDRED).intValue()));
        data.put("total_fee", String.valueOf(req.getOriginAmount().multiply(HUNDRED).intValue()));
        data.put("notify_url", wxPaymentConfig.getRefundNotifyUrl());
        try {
            WXPay wxPay = getClient(req.getFrom());
            Map<String, String> resp = wxPay.refund(data);
            checkResponse(resp, "refund");
        } catch (Exception e) {
            logger.error("Failed to refund", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    @Override
    public RefundQueryResult queryRefund(RefundQueryReq req) {
        Map<String, String> data = new HashMap<>();
        data.put("out_refund_no", req.getRefundRequestNo());
        try {
            WXPay wxPay = getClient(req.getFrom());
            Map<String, String> resp = wxPay.refundQuery(data);
            checkResponse(resp, "query refund");

            RefundQueryResult result = new RefundQueryResult();
            result.setRequestNo(resp.get("out_refund_no_0"));
            String tradeState = resp.get("refund_status_0");
            if ("SUCCESS".equals(tradeState)) {
                result.setState(RefundStateEnum.SUCCESS);
                result.setTradeNo(resp.get("refund_id_0"));
                result.setTradeTime(WXPaymentUtil.parseDateForRefund(resp.get("refund_success_time_0")));
                result.setAmount(new BigDecimal(resp.get("refund_fee_0")).divide(HUNDRED));
                // 扣除代金券后退款金额 settlement_refund_fee_1
            } else if ("REFUNDCLOSE".equals(tradeState)) {
                result.setState(RefundStateEnum.CANCELLED);
            } else if ("PROCESSING".equals(tradeState)) {
                result.setState(RefundStateEnum.IN_PROCESS);
            } else if ("CHANGE".equals(tradeState)) {
                result.setState(RefundStateEnum.PENDING);
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to refund", e);
            throw new PaymentException("Failed to request payment");
        }
    }

    private WXPayPrepareResult generateParam(Map<String, String> resp, PayFromEnum from) throws Exception {
        WXPayPrepareResult result = new WXPayPrepareResult();
        String appId = resp.get("appid");
        String prepayId = resp.get("prepay_id");
        String nonceStr = WXPayUtil.generateNonceStr();
        String timeStamp = String.valueOf(WXPayUtil.getCurrentTimestamp());

        result.setAppId(appId);
        result.setNonceStr(nonceStr);
        result.setTimeStamp(timeStamp);

        SignType signType = getSignType();
        Map<String, String> param = new HashMap<>();
        if (PayFromEnum.APP.equals(from)) {
            // refer to https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=8_3
            // sign(appid(应用ID)/partnerid(商户号)/prepayid/package(Sign=WXPay)/noncestr/timestamp)
            String mchId = resp.get("mch_id");
            param.put("appid", appId);
            param.put("partnerid", mchId);
            param.put("prepayid", prepayId);
            param.put("package", "Sign=WXPay");
            param.put("noncestr", nonceStr);
            param.put("timestamp", timeStamp);
            String sign = WXPayUtil.generateSignature(param, wxPaymentConfig.getKey(), signType);

            // refer to https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2&index=2
            // client require: appid(应用ID)/partnerid(商户号)/prepayid/package(Sign=WXPay)/noncestr/timestamp/sign
            result.setMchId(mchId);
            result.setPrepayId(prepayId);
            result.setPackageStr("Sign=WXPay");
            result.setPaySign(sign);
        } else { // WEB/H5/IN_APP
            // refer to https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=7_7&index=6
            // sign(appId/timeStamp/nonceStr/package(prepay_id=xxx)/signType)
            param.put("appId", appId);
            param.put("timeStamp", timeStamp);
            param.put("nonceStr", nonceStr);
            param.put("package", "prepay_id=" + prepayId);
            param.put("signType", signType.toString());
            String sign = WXPayUtil.generateSignature(param, wxPaymentConfig.getKey(), signType);

            // client require: appId/timeStamp/nonceStr/package/signType/paySign
            result.setPackageStr("prepay_id=" + prepayId);
            result.setSignType(signType.toString());
            result.setPaySign(sign);
        }


        return result;
    }

    public String payCallback(String respXml) {
        logger.info("payCallback respXml:" + respXml);
        String xml = "";
        try {
            Map<String, String> map = WXPayUtil.xmlToMap(respXml);
            if (!WXPayUtil.isSignatureValid(map, wxPaymentConfig.getKey(), getSignType())) {
                logger.error("签名不通过：{}", map);
                return xml;
            }
            if (!WXPayConstants.SUCCESS.equals(map.get(RETURN_CODE))) {
                logger.warn("支付通知失败：{}", map);
                return xml;
            }

            PayResp resp = new PayResp();
            // 失败应该不会异步通知
            resp.setSuccess(WXPayConstants.SUCCESS.equals(map.get(RESULT_CODE)));
            if (resp.isSuccess()) {
                resp.setTradeNo(map.get("transaction_id"));
                resp.setRequestNo(map.get("out_trade_no"));
                resp.setAmount(new BigDecimal(map.get("cash_fee")).divide(HUNDRED));
                resp.setTradeTime(WXPaymentUtil.parseDateForPay(map.get("time_end")));
            } else {
                resp.setErrorCode(map.get(ERR_CODE));
                resp.setErrorMsg(map.get(ERR_CODE_DES));
                logger.warn("支付通知结果为失败：{}", resp);
            }

            paymentBridge.notifyPayResult(resp);

            Map<String, String> param = new HashMap<>();
            param.put(RETURN_CODE, WXPayConstants.SUCCESS);
            param.put(RETURN_MSG, "OK");

            xml = WXPayUtil.mapToXml(param);
        } catch (Exception e) {
            logger.error("Failed to process pay callback", e);
        }

        return xml;
    }

    public String refundCallback(String encryptedText) {
        String xml = "";
        try {
            Map<String, String> map = WXPayUtil.xmlToMap(encryptedText);
            if (!WXPayConstants.SUCCESS.equals(map.get(RETURN_CODE))) {
                logger.warn("退款通知失败：{}", map);
                return xml;
            }

            String decrypted = WXPaymentUtil.decrypt(map.get("req_info"), wxPaymentConfig.getKey());

            Map<String, String> resultMap = WXPayUtil.xmlToMap(decrypted);
            String status = resultMap.get("refund_status");
            // 不是最终状态不进行通知
            RefundResp resp = new RefundResp();
            if (WXPayConstants.SUCCESS.equals(status) || "REFUNDCLOSE".equals(status)) {
                resp.setSuccess(WXPayConstants.SUCCESS.equals(status));
                resp.setRequestNo(resultMap.get("out_refund_no"));
                resp.setTradeNo(resultMap.get("refund_id"));
                if (resp.isSuccess()) {
                    resp.setAmount(new BigDecimal(resultMap.get("refund_fee")).divide(HUNDRED));
                    resp.setTradeTime(WXPaymentUtil.parseDateForRefund(resultMap.get("success_time")));
                } else {
                    resp.setErrorCode("REFUNDCLOSE");
                    resp.setErrorMsg("退款关闭");
                }

                paymentBridge.notifyRefundResult(resp);
            } else {
                logger.warn("退款通知结果为失败：{}", resp);
            }

            Map<String, String> param = new HashMap<>();
            param.put(RETURN_CODE, WXPayConstants.SUCCESS);

            xml = WXPayUtil.mapToXml(param);
        } catch (Exception e) {
            logger.error("Failed to process refund callback", e);
        }
        return xml;
    }

}