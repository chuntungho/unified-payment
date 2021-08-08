/*
 * Copyright (c) 2020-2021 Chuntung Ho. Some rights reserved.
 */

package com.chuntung.payment.dto;

public enum RefundStateEnum {
    /**
     * 处理中
     */
    IN_PROCESS,
    /**
     * 异常待处理
     */
    PENDING,
    /**
     * 退款成功
     */
    SUCCESS,
    /**
     * 撤销退款
     */
    CANCELLED
}
