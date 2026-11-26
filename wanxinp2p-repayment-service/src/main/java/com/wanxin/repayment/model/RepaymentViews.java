package com.wanxin.repayment.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 页面展示用的格式: 金额为两位小数字符串, id 为字符串(雪花 id 超出 JS 安全整数)
 */
final class RepaymentViews {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private RepaymentViews() {
    }

    static String money(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    static String id(Long value) {
        return value == null ? null : value.toString();
    }

    static String date(LocalDateTime value) {
        return value == null ? null : value.format(DATE);
    }

    static String dateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME);
    }
}
