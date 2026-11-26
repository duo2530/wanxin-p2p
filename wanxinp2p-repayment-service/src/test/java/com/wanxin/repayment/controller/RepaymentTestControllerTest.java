package com.wanxin.repayment.controller;

import com.wanxin.repayment.model.RepaymentExecution;
import com.wanxin.repayment.service.RepaymentService;
import org.junit.Test;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class RepaymentTestControllerTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(MockService.class, RepaymentTestController.class);

    @Test
    public void endpointIsAbsentUnlessEnabled() {
        runner.run(context -> assertThat(context).doesNotHaveBean(RepaymentTestController.class));
        runner.withPropertyValues("repayment.test-endpoint.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RepaymentTestController.class));
    }

    @Test
    public void endpointIsEnabledByTheDocumentedEnvironmentVariable() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        Collections.singletonMap("REPAYMENT_TESTENDPOINT_ENABLED", "true")));
        ConfigurationPropertySources.attach(environment);

        assertThat(environment.getProperty("repayment.test-endpoint.enabled")).isEqualTo("true");
    }

    @Test
    public void enabledEndpointReportsDueAndSubmittedCounts() {
        runner.withPropertyValues("repayment.test-endpoint.enabled=true").run(context -> {
            RepaymentService service = context.getBean(RepaymentService.class);
            when(service.executeRepayment("2026-12-26")).thenReturn(new RepaymentExecution("2026-12-26", 2, 1));
            MockMvc mvc = MockMvcBuilders.standaloneSetup(context.getBean(RepaymentTestController.class)).build();

            mvc.perform(get("/execute-repayment/2026-12-26"))
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.result.date").value("2026-12-26"))
                    .andExpect(jsonPath("$.result.dueCount").value(2))
                    .andExpect(jsonPath("$.result.submittedCount").value(1));
        });
    }

    @Test
    public void malformedDateDoesNotRunRepayment() {
        runner.withPropertyValues("repayment.test-endpoint.enabled=true").run(context -> {
            MockMvc mvc = MockMvcBuilders.standaloneSetup(context.getBean(RepaymentTestController.class)).build();

            mvc.perform(get("/execute-repayment/2026-12-26x")).andExpect(jsonPath("$.code").value(-2));
            mvc.perform(get("/execute-repayment/2026-02-30")).andExpect(jsonPath("$.code").value(-2));
            verifyZeroInteractions(context.getBean(RepaymentService.class));
        });
    }

    @Configuration
    static class MockService {
        @Bean
        RepaymentService repaymentService() {
            return mock(RepaymentService.class);
        }
    }
}
