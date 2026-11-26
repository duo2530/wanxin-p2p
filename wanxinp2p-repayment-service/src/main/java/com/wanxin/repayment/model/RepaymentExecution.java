package com.wanxin.repayment.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 一次按日期执行还款的结果
 */
@Data
@AllArgsConstructor
public class RepaymentExecution {
    private String date;
    /**
     * 当天到期且待还的计划条数
     */
    private int dueCount;
    /**
     * 存管冻结成功、已发出确认还款消息的条数
     */
    private int submittedCount;
}
