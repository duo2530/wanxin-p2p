package com.wanxin.repayment.model;

import lombok.Data;

/**
 * 后台还款计划查询条件, 字段为空表示不筛选
 */
@Data
public class RepaymentPlanQuery {
    /**
     * 还款状态 0.待还 1.已还 2.部分还款
     */
    private String repaymentStatus;
    /**
     * 应还日期 yyyy-MM-dd
     */
    private String shouldRepaymentDate;
    /**
     * 标的号
     */
    private String projectNo;
    /**
     * 只看还款中的借款: 至少已还一期且仍有待还期
     */
    private Boolean inProgress;
}
