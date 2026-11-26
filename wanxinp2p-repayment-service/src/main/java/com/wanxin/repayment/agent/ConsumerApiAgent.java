package com.wanxin.repayment.agent;

import com.wanxin.api.consumer.model.ConsumerDTO;
import com.wanxin.common.domain.BusinessException;
import com.wanxin.common.domain.CommonErrorCode;
import com.wanxin.common.domain.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务
 */
@FeignClient(
        value = "consumer-service",
        fallback = ConsumerApiAgentFallback.class,
        configuration = {ConsumerApiAgentConfiguration.class}
)
public interface ConsumerApiAgent {
    /**
     * 获取当前登录用户信息
     * <p>
     * jsonToken 显式传递: 本服务未配置 hystrix 信号量隔离, feign 调用在 hystrix 线程中拿不到当前请求头
     *
     * @param jsonToken 网关写入的登录用户信息
     * @return
     */
    @GetMapping("/consumer/my/consumers")
    RestResponse<ConsumerDTO> getCurrentLoginConsumer(@RequestHeader("jsonToken") String jsonToken);
}

class ConsumerApiAgentFallback implements ConsumerApiAgent {
    @Override
    public RestResponse<ConsumerDTO> getCurrentLoginConsumer(String jsonToken) {
        throw new BusinessException(CommonErrorCode.E_999992);
    }
}

class ConsumerApiAgentConfiguration {
    @Bean
    public ConsumerApiAgentFallback consumerApiAgentFallback() {
        return new ConsumerApiAgentFallback();
    }
}
