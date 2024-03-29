/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto.wxpay;

import java.io.Serializable;

public class WXPayPrepareResult implements Serializable {
	private static final long serialVersionUID = -5237529955589503064L;

	private String appId;
	private String mchId;
	private String prepayId;
	private String timeStamp;
	private String nonceStr;
	private String packageStr;
	private String signType;
	private String paySign;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getMchId() {
		return mchId;
	}

	public void setMchId(String mchId) {
		this.mchId = mchId;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	public String getPackageStr() {
		return packageStr;
	}

	public void setPackageStr(String packageStr) {
		this.packageStr = packageStr;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getPaySign() {
		return paySign;
	}

	public void setPaySign(String paySign) {
		this.paySign = paySign;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("WXPayPrepareResult{");
		sb.append("appId='").append(appId).append('\'');
		sb.append(", mchId='").append(mchId).append('\'');
		sb.append(", prepayId='").append(prepayId).append('\'');
		sb.append(", timeStamp='").append(timeStamp).append('\'');
		sb.append(", nonceStr='").append(nonceStr).append('\'');
		sb.append(", packageStr='").append(packageStr).append('\'');
		sb.append(", signType='").append(signType).append('\'');
		sb.append(", paySign='").append(paySign).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
