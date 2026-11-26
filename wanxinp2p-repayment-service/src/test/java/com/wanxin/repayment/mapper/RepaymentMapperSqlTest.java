package com.wanxin.repayment.mapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.pagination.optimize.JsqlParserCountOptimize;
import com.wanxin.repayment.model.RepaymentPlanQuery;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 不连数据库: 验证注解 SQL 按条件渲染、能被 JSqlParser 解析, 且分页插件能把它优化成 COUNT 语句
 */
public class RepaymentMapperSqlTest {
    private static final MybatisConfiguration CONFIGURATION = new MybatisConfiguration();

    @BeforeClass
    public static void registerMappers() {
        CONFIGURATION.addMapper(RepaymentPlanMapper.class);
        CONFIGURATION.addMapper(RepaymentDetailMapper.class);
    }

    @Test
    public void planPageWithoutFiltersHasNoWhereClause() throws Exception {
        BoundSql sql = planPage(new RepaymentPlanQuery());

        assertThat(sql.getSql()).doesNotContain("WHERE p.").endsWith("ORDER BY p.SHOULD_REPAYMENT_DATE, p.PROJECT_NO, p.NUMBER_OF_PERIODS");
        assertThat(sql.getParameterMappings()).isEmpty();
        assertParsesAndCounts(sql.getSql(), "repayment_plan p");
    }

    @Test
    public void planPageAppliesEveryFilter() throws Exception {
        RepaymentPlanQuery query = new RepaymentPlanQuery();
        query.setRepaymentStatus("0");
        query.setShouldRepaymentDate("2026-12-26");
        query.setProjectNo("PRO_1");
        query.setInProgress(true);

        BoundSql sql = planPage(query);

        assertThat(sql.getSql())
                .contains("p.REPAYMENT_STATUS = ?")
                .contains("DATE_FORMAT(p.SHOULD_REPAYMENT_DATE, '%Y-%m-%d') = ?")
                .contains("p.PROJECT_NO = ?")
                .contains("HAVING SUM(CASE WHEN r.REPAYMENT_STATUS = '1' THEN 1 ELSE 0 END) > 0")
                .contains("SUM(CASE WHEN r.REPAYMENT_STATUS = '0' THEN 1 ELSE 0 END) > 0");
        assertThat(properties(sql)).containsExactly("q.repaymentStatus", "q.shouldRepaymentDate", "q.projectNo");
        assertParsesAndCounts(sql.getSql(), "repayment_plan p");
    }

    @Test
    public void inProgressFalseIsNotAFilter() {
        RepaymentPlanQuery query = new RepaymentPlanQuery();
        query.setInProgress(false);

        assertThat(planPage(query).getSql()).doesNotContain("HAVING");
    }

    @Test
    public void consumerPlansAreScopedToTheConsumer() throws Exception {
        String statement = RepaymentPlanMapper.class.getName() + ".selectConsumerPlans";
        Map<String, Object> params = new HashMap<>();
        params.put("consumerId", 7L);
        params.put("projectId", null);
        BoundSql all = CONFIGURATION.getMappedStatement(statement).getBoundSql(params);
        params.put("projectId", 9L);
        BoundSql one = CONFIGURATION.getMappedStatement(statement).getBoundSql(params);

        assertThat(all.getSql()).contains("WHERE p.CONSUMER_ID = ?");
        assertThat(properties(all)).containsExactly("consumerId");
        assertThat(properties(one)).containsExactly("consumerId", "projectId");
        CCJSqlParserUtil.parse(one.getSql());
    }

    @Test
    public void consumerRecordPageCountsJoinedRows() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("consumerId", 7L);
        BoundSql sql = CONFIGURATION.getMappedStatement(RepaymentDetailMapper.class.getName() + ".selectConsumerRecordPage").getBoundSql(params);

        assertThat(sql.getSql()).contains("WHERE d.STATUS = 1 AND p.CONSUMER_ID = ?");
        assertParsesAndCounts(sql.getSql(), "repayment_detail d INNER JOIN repayment_plan p");
    }

    private static BoundSql planPage(RepaymentPlanQuery query) {
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        return CONFIGURATION.getMappedStatement(RepaymentPlanMapper.class.getName() + ".selectPlanPage").getBoundSql(params);
    }

    private static List<String> properties(BoundSql sql) {
        return sql.getParameterMappings().stream().map(ParameterMapping::getProperty).collect(Collectors.toList());
    }

    private static void assertParsesAndCounts(String sql, String from) throws Exception {
        CCJSqlParserUtil.parse(sql);
        String countSql = new JsqlParserCountOptimize(true).parser(null, sql).getSql();
        assertThat(countSql).startsWith("SELECT COUNT(1) FROM " + from);
    }
}
