package com.wanxin.repayment.model;

import lombok.Data;

/**
 * 还款计划(一期)
 */
@Data
public class RepaymentPlanVO {
    private String id;
    private String projectId;
    private String projectNo;
    private String userNo;
    private Integer numberOfPeriods;
    private String principal;
    private String interest;
    private String amount;
    private String shouldRepaymentDate;
    private String repaymentStatus;
    private String repaymentStatusName;
    private String repaymentDate;

    public static RepaymentPlanVO from(RepaymentPlanRow row) {
        RepaymentPlanVO vo = new RepaymentPlanVO();
        vo.setId(RepaymentViews.id(row.getId()));
        vo.setProjectId(RepaymentViews.id(row.getProjectId()));
        vo.setProjectNo(row.getProjectNo());
        vo.setUserNo(row.getUserNo());
        vo.setNumberOfPeriods(row.getNumberOfPeriods());
        vo.setPrincipal(RepaymentViews.money(row.getPrincipal()));
        vo.setInterest(RepaymentViews.money(row.getInterest()));
        vo.setAmount(RepaymentViews.money(row.getAmount()));
        vo.setShouldRepaymentDate(RepaymentViews.date(row.getShouldRepaymentDate()));
        vo.setRepaymentStatus(row.getRepaymentStatus());
        vo.setRepaymentStatusName(RepaymentStatus.displayNameOf(row.getRepaymentStatus()));
        vo.setRepaymentDate(RepaymentViews.dateTime(row.getRepaymentDate()));
        return vo;
    }
}
