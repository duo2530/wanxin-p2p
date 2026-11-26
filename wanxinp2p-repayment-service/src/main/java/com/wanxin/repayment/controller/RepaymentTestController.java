package com.wanxin.repayment.controller;

import com.wanxin.common.domain.RestResponse;
import com.wanxin.repayment.model.RepaymentExecution;
import com.wanxin.repayment.service.RepaymentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试环境手动触发到期还款。
 * <p>
 * 网关对本服务不做登录校验, 任何能访问网关的人都能触发全量扣款, 所以默认不注册,
 * 只有 repayment.test-endpoint.enabled=true(环境变量 REPAYMENT_TESTENDPOINT_ENABLED=true)时才存在。
 */
@RestController
@ConditionalOnProperty(name = "repayment.test-endpoint.enabled", havingValue = "true")
@Api(value = "还款测试", tags = "repayment-test")
public class RepaymentTestController {
    @Autowired
    private RepaymentService repaymentService;

    @GetMapping("/execute-repayment/{date}")
    @ApiOperation("执行应还日期为 date 的全部待还计划(测试用)")
    @ApiImplicitParam(name = "date", value = "应还日期 yyyy-MM-dd", required = true, dataType = "string", paramType = "path")
    public RestResponse<RepaymentExecution> executeRepayment(@PathVariable("date") String date) {
        if (!RepaymentController.isIsoDate(date)) {
            return RestResponse.validfail("日期格式应为 yyyy-MM-dd");
        }
        return RestResponse.success(repaymentService.executeRepayment(date));
    }
}
