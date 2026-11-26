package com.wanxin.repayment.service;

import com.wanxin.api.depository.model.UserAutoPreTransactionRequest;
import com.wanxin.api.repayment.model.RepaymentRequest;
import com.wanxin.common.domain.DepositoryReturnCode;
import com.wanxin.common.domain.RestResponse;
import com.wanxin.repayment.agent.DepositoryAgentApiAgent;
import com.wanxin.repayment.entity.RepaymentDetail;
import com.wanxin.repayment.entity.RepaymentPlan;
import com.wanxin.repayment.mapper.ReceivableDetailMapper;
import com.wanxin.repayment.mapper.ReceivablePlanMapper;
import com.wanxin.repayment.mapper.RepaymentDetailMapper;
import com.wanxin.repayment.mapper.RepaymentPlanMapper;
import com.wanxin.repayment.message.RepaymentProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RepaymentServiceImplTest {
    @Mock
    private RepaymentPlanMapper planMapper;
    @Mock
    private ReceivablePlanMapper receivablePlanMapper;
    @Mock
    private RepaymentDetailMapper repaymentDetailMapper;
    @Mock
    private ReceivableDetailMapper receivableDetailMapper;
    @Mock
    private DepositoryAgentApiAgent depositoryAgentApiAgent;
    @Mock
    private RepaymentProducer repaymentProducer;
    @InjectMocks
    private RepaymentServiceImpl repaymentService;

    @Test
    public void executeRepaymentFreezesAndConfirmsTheDuePlan() {
        RepaymentPlan plan = new RepaymentPlan();
        plan.setId(1001L);
        plan.setUserNo("USR_BORROWER");
        plan.setProjectNo("PRO_1");
        plan.setNumberOfPeriods(1);
        plan.setPrincipal(new BigDecimal("79.51"));
        plan.setInterest(new BigDecimal("20.90"));
        plan.setAmount(new BigDecimal("100.41"));
        plan.setShouldRepaymentDate(LocalDateTime.of(2026, 12, 26, 10, 0));
        plan.setRepaymentStatus("0");
        when(planMapper.selectDueRepayment("2026-12-26")).thenReturn(Collections.singletonList(plan));
        when(receivablePlanMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(depositoryAgentApiAgent.userAutoPreTransaction(any()))
                .thenReturn(RestResponse.success(DepositoryReturnCode.RETURN_CODE_00000.getCode()));

        repaymentService.executeRepayment("2026-12-26");

        ArgumentCaptor<RepaymentDetail> detail = ArgumentCaptor.forClass(RepaymentDetail.class);
        verify(repaymentDetailMapper).insert(detail.capture());
        assertEquals(Long.valueOf(1001L), detail.getValue().getRepaymentPlanId());
        assertEquals(new BigDecimal("100.41"), detail.getValue().getAmount());

        ArgumentCaptor<UserAutoPreTransactionRequest> freeze = ArgumentCaptor.forClass(UserAutoPreTransactionRequest.class);
        verify(depositoryAgentApiAgent).userAutoPreTransaction(freeze.capture());
        assertEquals(new BigDecimal("100.41"), freeze.getValue().getAmount());
        assertEquals("USR_BORROWER", freeze.getValue().getUserNo());
        assertEquals("PRO_1", freeze.getValue().getProjectNo());
        assertEquals(Long.valueOf(1001L), freeze.getValue().getId());

        ArgumentCaptor<RepaymentPlan> confirmedPlan = ArgumentCaptor.forClass(RepaymentPlan.class);
        ArgumentCaptor<RepaymentRequest> confirm = ArgumentCaptor.forClass(RepaymentRequest.class);
        verify(repaymentProducer).confirmRepayment(confirmedPlan.capture(), confirm.capture());
        assertEquals(Long.valueOf(1001L), confirmedPlan.getValue().getId());
        assertEquals(new BigDecimal("100.41"), confirm.getValue().getAmount());
        assertEquals("PRO_1", confirm.getValue().getProjectNo());
        assertEquals(detail.getValue().getRequestNo(), confirm.getValue().getPreRequestNo());
    }
}
