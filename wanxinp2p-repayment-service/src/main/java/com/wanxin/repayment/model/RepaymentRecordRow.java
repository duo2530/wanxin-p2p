package com.wanxin.repayment.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 已完成的一次还款
 */
@Data
public class RepaymentRecordRow {
    private Long projectId;
    private String projectNo;
    private Integer numberOfPeriods;
    private BigDecimal amount;
    private LocalDateTime repaymentDate;
}
