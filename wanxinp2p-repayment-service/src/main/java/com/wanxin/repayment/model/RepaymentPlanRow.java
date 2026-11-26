package com.wanxin.repayment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 还款计划及其实际还款时间的查询结果
 */
@Data
public class RepaymentPlanRow {
    private Long id;
    private Long projectId;
    private String projectNo;
    private String userNo;
    private Integer numberOfPeriods;
    private BigDecimal principal;
    private BigDecimal interest;
    private BigDecimal amount;
    private LocalDateTime shouldRepaymentDate;
    private String repaymentStatus;
    private LocalDateTime repaymentDate;
}
