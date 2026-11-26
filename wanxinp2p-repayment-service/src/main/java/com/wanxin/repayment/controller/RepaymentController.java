package com.wanxin.repayment.controller;

import com.wanxin.api.consumer.model.ConsumerDTO;
import com.wanxin.api.repayment.RepaymentAPI;
import com.wanxin.api.transaction.model.ProjectWithTendersDTO;
import com.wanxin.common.domain.CommonErrorCode;
import com.wanxin.common.domain.PageVO;
import com.wanxin.common.domain.RestResponse;
import com.wanxin.repayment.agent.ConsumerApiAgent;
import com.wanxin.repayment.model.RepaymentPlanQuery;
import com.wanxin.repayment.model.RepaymentPlanVO;
import com.wanxin.repayment.model.RepaymentRecordVO;
import com.wanxin.repayment.model.RepaymentStatus;
import com.wanxin.repayment.service.RepaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * @author yuelimin
 * @version 1.0.0
 * @since 1.8
 */
@RestController
@Api(value = "还款服务", tags = "repayment")
public class RepaymentController implements RepaymentAPI {
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private RepaymentService repaymentService;
    @Autowired
    private ConsumerApiAgent consumerApiAgent;

    @Override
    @PostMapping("/l/start-repayment")
    @ApiOperation("启动还款")
    @ApiImplicitParam(name = "projectWithTendersDTO", value = "通过id获取标的信息", required = true, dataType = "ProjectWithTendersDTO", paramType = "body")
    public RestResponse<String> startRepayment(@RequestBody ProjectWithTendersDTO projectWithTendersDTO) {
        return RestResponse.success(repaymentService.startRepayment(projectWithTendersDTO));
    }

    @PostMapping("/my/repaymentRecords/q")
    @ApiOperation("借款人查询自己已完成的还款记录")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "页码, 从 1 开始", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataType = "int", paramType = "query")
    })
    public RestResponse<PageVO<RepaymentRecordVO>> queryMyRepaymentRecords(
            @RequestHeader(value = "jsonToken", required = false) String jsonToken,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long consumerId = currentConsumerId(jsonToken);
        if (consumerId == null) {
            return notLoggedIn();
        }
        return RestResponse.success(repaymentService.queryConsumerRepaymentRecords(consumerId, pageNo(pageNo), pageSize(pageSize)));
    }

    @GetMapping("/my/repayment-plans")
    @ApiOperation("借款人查询自己的还款计划")
    @ApiImplicitParam(name = "projectId", value = "标的id, 不传查全部借款", dataType = "long", paramType = "query")
    public RestResponse<List<RepaymentPlanVO>> queryMyRepaymentPlans(
            @RequestHeader(value = "jsonToken", required = false) String jsonToken,
            @RequestParam(value = "projectId", required = false) Long projectId) {
        Long consumerId = currentConsumerId(jsonToken);
        if (consumerId == null) {
            return notLoggedIn();
        }
        return RestResponse.success(repaymentService.queryConsumerRepaymentPlans(consumerId, projectId));
    }

    @PostMapping("/m/repayment-plans/q")
    @ApiOperation("管理员分页查询还款计划")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "query", value = "查询条件, 字段为空不筛选", dataType = "RepaymentPlanQuery", paramType = "body"),
            @ApiImplicitParam(name = "pageNo", value = "页码, 从 1 开始", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页记录数", dataType = "int", paramType = "query")
    })
    public RestResponse<PageVO<RepaymentPlanVO>> queryRepaymentPlans(
            @RequestBody(required = false) RepaymentPlanQuery query,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        RepaymentPlanQuery condition = query == null ? new RepaymentPlanQuery() : query;
        condition.setRepaymentStatus(StringUtils.trimToNull(condition.getRepaymentStatus()));
        condition.setShouldRepaymentDate(StringUtils.trimToNull(condition.getShouldRepaymentDate()));
        condition.setProjectNo(StringUtils.trimToNull(condition.getProjectNo()));
        if (condition.getRepaymentStatus() != null && !RepaymentStatus.isCode(condition.getRepaymentStatus())) {
            return RestResponse.validfail("还款状态只能是 0(待还)、1(已还)、2(部分还款)");
        }
        if (condition.getShouldRepaymentDate() != null && !isIsoDate(condition.getShouldRepaymentDate())) {
            return RestResponse.validfail("应还日期格式应为 yyyy-MM-dd");
        }
        return RestResponse.success(repaymentService.queryRepaymentPlans(condition, pageNo(pageNo), pageSize(pageSize)));
    }

    static boolean isIsoDate(String date) {
        try {
            LocalDate.parse(date);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 网关只在请求带有效登录令牌时写入 jsonToken 头, 用户服务据此解析出当前用户
     */
    private Long currentConsumerId(String jsonToken) {
        if (StringUtils.isBlank(jsonToken)) {
            return null;
        }
        ConsumerDTO consumer = consumerApiAgent.getCurrentLoginConsumer(jsonToken).getResult();
        return consumer == null ? null : consumer.getId();
    }

    private static <T> RestResponse<T> notLoggedIn() {
        return new RestResponse<>(CommonErrorCode.CUSTOM.getCode(), "请先登录");
    }

    private static int pageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? 1 : pageNo;
    }

    private static int pageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
