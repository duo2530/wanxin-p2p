package com.wanxin.repayment.model;

import lombok.Data;

/**
 * 前台"还款记录"的一条
 */
@Data
public class RepaymentRecordVO {
    /**
     * 系统只有到期自动扣款这一种还款方式
     */
    private static final String AUTO_WITHHOLD = "自动还款";

    private String amount;
    private String repaymentDate;
    private String withholdWay;
    private String projectId;
    private String projectNo;
    private Integer numberOfPeriods;

    public static RepaymentRecordVO from(RepaymentRecordRow row) {
        RepaymentRecordVO vo = new RepaymentRecordVO();
        vo.setAmount(RepaymentViews.money(row.getAmount()));
        vo.setRepaymentDate(RepaymentViews.dateTime(row.getRepaymentDate()));
        vo.setWithholdWay(AUTO_WITHHOLD);
        vo.setProjectId(RepaymentViews.id(row.getProjectId()));
        vo.setProjectNo(row.getProjectNo());
        vo.setNumberOfPeriods(row.getNumberOfPeriods());
        return vo;
    }
}
