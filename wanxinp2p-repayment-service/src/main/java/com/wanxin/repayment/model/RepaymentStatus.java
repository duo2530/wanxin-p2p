package com.wanxin.repayment.model;

import java.util.Arrays;

/**
 * repayment_plan.REPAYMENT_STATUS 的取值
 */
public enum RepaymentStatus {
    PENDING("0", "待还"),
    PAID("1", "已还"),
    PARTIAL("2", "部分还款");

    private final String code;
    private final String displayName;

    RepaymentStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static boolean isCode(String code) {
        return Arrays.stream(values()).anyMatch(status -> status.code.equals(code));
    }

    public static String displayNameOf(String code) {
        return Arrays.stream(values()).filter(status -> status.code.equals(code))
                .findFirst().map(RepaymentStatus::getDisplayName).orElse(code);
    }
}
