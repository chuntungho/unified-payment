package com.chuntung.payment.service.impl.wxpay;

import com.github.wxpay.sdk.WXPayUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

public class WXPaymentUtil {
    private static final ThreadLocal<SimpleDateFormat> PAY_DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyyMMddHHmmss")
    );

    private static final ThreadLocal<SimpleDateFormat> REFUND_DATE_FORMAT = ThreadLocal.withInitial(
            () -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    );

    // refer to https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_16&index=10
    public static String decrypt(String base64, String key) throws Exception {
        // AES decrypt decode base64 with MD5(key)
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(WXPayUtil.MD5(key).toLowerCase().getBytes(), "AES"));

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decryptStrBytes = cipher.doFinal(decoder.decode(base64));
        return new String(decryptStrBytes, "UTF-8");
    }

    public static Date parseDateForPay(String dateStr) {
        try {
            return PAY_DATE_FORMAT.get().parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDateForRefund(String dateStr) {
        try {
            return REFUND_DATE_FORMAT.get().parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}