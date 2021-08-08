/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto;

public enum PayStateEnum {
    /**
     * 初始化
     */
    INIT,
    /**
     * 用户支付中
     */
    PAYING,
    /**
     * 支付成功
     */
    SUCCESS,
    /**
     * 支付失败
     */
    FAILED,
    /**
     * 撤销支付
     */
    CANCELED
}
