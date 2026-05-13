package com.greatmall.constant;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING_PAY(1, "待支付"),
    PAID(2, "已支付"),
    CANCELED(3, "已取消"),
    FAILED(4, "创建失败");

    private final Integer code;
    private final String desc;

    OrderStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status.desc;
            }
        }
        return "未知状态";
    }
}

