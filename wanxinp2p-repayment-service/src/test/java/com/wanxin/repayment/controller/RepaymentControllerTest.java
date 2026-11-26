package com.wanxin.repayment.controller;

import com.wanxin.api.consumer.model.ConsumerDTO;
import com.wanxin.common.domain.PageVO;
import com.wanxin.common.domain.RestResponse;
import com.wanxin.repayment.agent.ConsumerApiAgent;
import com.wanxin.repayment.model.RepaymentPlanQuery;
import com.wanxin.repayment.model.RepaymentPlanRow;
import com.wanxin.repayment.model.RepaymentPlanVO;
import com.wanxin.repayment.model.RepaymentRecordRow;
import com.wanxin.repayment.model.RepaymentRecordVO;
import com.wanxin.repayment.service.RepaymentService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class RepaymentControllerTest {
    private static final String TOKEN = "eyJtb2JpbGUiOiIxMzgwMDAwMDAwMCJ9";

    @Mock
    private RepaymentService repaymentService;
    @Mock
    private ConsumerApiAgent consumerApiAgent;
    @InjectMocks
    private RepaymentController controller;

    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void myRecordsRequireLogin() throws Exception {
        mvc.perform(post("/my/repaymentRecords/q?pageNo=1&pageSize=10").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(999998))
                .andExpect(jsonPath("$.msg").value("请先登录"));
        verifyZeroInteractions(repaymentService, consumerApiAgent);
    }

    @Test
    public void myRecordsAreTheLoggedInBorrowersRepayments() throws Exception {
        loggedInAs(7L);
        RepaymentRecordRow row = new RepaymentRecordRow();
        row.setProjectId(1330400473113923585L);
        row.setProjectNo("PRO_1");
        row.setNumberOfPeriods(1);
        row.setAmount(new BigDecimal("100.40"));
        row.setRepaymentDate(LocalDateTime.of(2026, 11, 27, 10, 15, 2));
        when(repaymentService.queryConsumerRepaymentRecords(7L, 1, 10))
                .thenReturn(new PageVO<>(Collections.singletonList(RepaymentRecordVO.from(row)), 1, 1, 10));

        mvc.perform(post("/my/repaymentRecords/q?pageNo=1&pageSize=10&order=asc&sortBy=")
                .header("jsonToken", TOKEN).contentType(MediaType.APPLICATION_JSON)
                .content("{\"pageNo\":1,\"pageSize\":10,\"order\":\"asc\",\"sortBy\":\"\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.result.total").value(1))
                .andExpect(jsonPath("$.result.content[0].amount").value("100.40"))
                .andExpect(jsonPath("$.result.content[0].repaymentDate").value("2026-11-27 10:15:02"))
                .andExpect(jsonPath("$.result.content[0].withholdWay").value("自动还款"))
                .andExpect(jsonPath("$.result.content[0].projectId").value("1330400473113923585"))
                .andExpect(jsonPath("$.result.content[0].numberOfPeriods").value(1));
        verify(consumerApiAgent).getCurrentLoginConsumer(TOKEN);
    }

    @Test
    public void myPlansAreFilteredByTheLoggedInBorrowerAndProject() throws Exception {
        loggedInAs(7L);
        when(repaymentService.queryConsumerRepaymentPlans(7L, 1330400473113923585L))
                .thenReturn(Collections.singletonList(RepaymentPlanVO.from(pendingPlan())));

        mvc.perform(get("/my/repayment-plans?projectId=1330400473113923585").header("jsonToken", TOKEN))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.result[0].id").value("1330400480000000002"))
                .andExpect(jsonPath("$.result[0].numberOfPeriods").value(2))
                .andExpect(jsonPath("$.result[0].shouldRepaymentDate").value("2027-01-26"))
                .andExpect(jsonPath("$.result[0].amount").value("100.41"))
                .andExpect(jsonPath("$.result[0].principal").value("80.30"))
                .andExpect(jsonPath("$.result[0].interest").value("20.11"))
                .andExpect(jsonPath("$.result[0].repaymentStatus").value("0"))
                .andExpect(jsonPath("$.result[0].repaymentStatusName").value("待还"))
                .andExpect(jsonPath("$.result[0].repaymentDate").doesNotExist());
    }

    @Test
    public void myPlansRequireAKnownConsumer() throws Exception {
        when(consumerApiAgent.getCurrentLoginConsumer(TOKEN)).thenReturn(RestResponse.success(null));

        mvc.perform(get("/my/repayment-plans").header("jsonToken", TOKEN))
                .andExpect(jsonPath("$.msg").value("请先登录"));
        verifyZeroInteractions(repaymentService);
    }

    @Test
    public void adminQueryPassesNormalizedFilters() throws Exception {
        RepaymentPlanRow paid = pendingPlan();
        paid.setRepaymentStatus("1");
        paid.setRepaymentDate(LocalDateTime.of(2026, 11, 27, 10, 15, 2));
        when(repaymentService.queryRepaymentPlans(any(), eq(2), eq(100)))
                .thenReturn(new PageVO<>(Collections.singletonList(RepaymentPlanVO.from(paid)), 1, 2, 100));

        mvc.perform(post("/m/repayment-plans/q?pageNo=2&pageSize=500").contentType(MediaType.APPLICATION_JSON)
                .content("{\"repaymentStatus\":\"1\",\"shouldRepaymentDate\":\"\",\"projectNo\":\" PRO_1 \"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.result.content[0].userNo").value("USR_1"))
                .andExpect(jsonPath("$.result.content[0].projectNo").value("PRO_1"))
                .andExpect(jsonPath("$.result.content[0].repaymentStatusName").value("已还"))
                .andExpect(jsonPath("$.result.content[0].repaymentDate").value("2026-11-27 10:15:02"));

        ArgumentCaptor<RepaymentPlanQuery> query = ArgumentCaptor.forClass(RepaymentPlanQuery.class);
        verify(repaymentService).queryRepaymentPlans(query.capture(), eq(2), eq(100));
        assertEquals("1", query.getValue().getRepaymentStatus());
        assertNull(query.getValue().getShouldRepaymentDate());
        assertEquals("PRO_1", query.getValue().getProjectNo());
    }

    @Test
    public void adminQueryWithoutBodyListsEverything() throws Exception {
        when(repaymentService.queryRepaymentPlans(any(), anyInt(), anyInt())).thenReturn(new PageVO<>());

        mvc.perform(post("/m/repayment-plans/q")).andExpect(jsonPath("$.code").value(0));

        ArgumentCaptor<RepaymentPlanQuery> query = ArgumentCaptor.forClass(RepaymentPlanQuery.class);
        verify(repaymentService).queryRepaymentPlans(query.capture(), eq(1), eq(10));
        assertEquals(new RepaymentPlanQuery(), query.getValue());
    }

    @Test
    public void adminQueryRejectsMalformedFilters() throws Exception {
        mvc.perform(post("/m/repayment-plans/q").contentType(MediaType.APPLICATION_JSON).content("{\"shouldRepaymentDate\":\"2026-1-5\"}"))
                .andExpect(jsonPath("$.code").value(-2));
        mvc.perform(post("/m/repayment-plans/q").contentType(MediaType.APPLICATION_JSON).content("{\"repaymentStatus\":\"paid\"}"))
                .andExpect(jsonPath("$.code").value(-2));
        verifyZeroInteractions(repaymentService);
    }

    private void loggedInAs(Long consumerId) {
        ConsumerDTO consumer = new ConsumerDTO();
        consumer.setId(consumerId);
        when(consumerApiAgent.getCurrentLoginConsumer(TOKEN)).thenReturn(RestResponse.success(consumer));
    }

    private static RepaymentPlanRow pendingPlan() {
        RepaymentPlanRow row = new RepaymentPlanRow();
        row.setId(1330400480000000002L);
        row.setProjectId(1330400473113923585L);
        row.setProjectNo("PRO_1");
        row.setUserNo("USR_1");
        row.setNumberOfPeriods(2);
        row.setPrincipal(new BigDecimal("80.30"));
        row.setInterest(new BigDecimal("20.11"));
        row.setAmount(new BigDecimal("100.41"));
        row.setShouldRepaymentDate(LocalDateTime.of(2027, 1, 26, 9, 30));
        row.setRepaymentStatus("0");
        return row;
    }
}
