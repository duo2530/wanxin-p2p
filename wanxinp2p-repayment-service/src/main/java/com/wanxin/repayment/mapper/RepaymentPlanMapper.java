package com.wanxin.repayment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanxin.repayment.entity.RepaymentPlan;
import com.wanxin.repayment.model.RepaymentPlanQuery;
import com.wanxin.repayment.model.RepaymentPlanRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 借款人还款计划 Mapper 接口
 * </p>
 *
 * @author yuelimin
 * @version 1.0.0
 * @since 1.8
 */
@Repository
public interface RepaymentPlanMapper extends BaseMapper<RepaymentPlan> {
    /**
     * 实际还款时间取已同步(STATUS = 1)的还款明细
     */
    String SELECT_PLAN_ROW = "SELECT p.ID AS id, p.PROJECT_ID AS projectId, p.PROJECT_NO AS projectNo, p.USER_NO AS userNo,"
            + " p.NUMBER_OF_PERIODS AS numberOfPeriods, p.PRINCIPAL AS principal, p.INTEREST AS interest, p.AMOUNT AS amount,"
            + " p.SHOULD_REPAYMENT_DATE AS shouldRepaymentDate, p.REPAYMENT_STATUS AS repaymentStatus,"
            + " (SELECT MAX(d.REPAYMENT_DATE) FROM repayment_detail d WHERE d.REPAYMENT_PLAN_ID = p.ID AND d.STATUS = 1) AS repaymentDate"
            + " FROM repayment_plan p";

    /**
     * 查询所有到期的还款计划
     *
     * @return
     */
    @Select("SELECT * FROM repayment_plan WHERE DATE_FORMAT(SHOULD_REPAYMENT_DATE, '%Y-%m-%d') = #{date} AND REPAYMENT_STATUS = '0'")
    List<RepaymentPlan> selectDueRepayment(@Param("date") String date);

    /**
     * 后台按条件分页查询还款计划
     *
     * @param page  分页
     * @param query 查询条件, 字段为空不筛选
     * @return
     */
    @Select({"<script>", SELECT_PLAN_ROW,
            "<where>",
            "<if test='q.repaymentStatus != null'>AND p.REPAYMENT_STATUS = #{q.repaymentStatus}</if>",
            "<if test='q.shouldRepaymentDate != null'>AND DATE_FORMAT(p.SHOULD_REPAYMENT_DATE, '%Y-%m-%d') = #{q.shouldRepaymentDate}</if>",
            "<if test='q.projectNo != null'>AND p.PROJECT_NO = #{q.projectNo}</if>",
            "<if test='q.inProgress == true'>AND p.PROJECT_ID IN (SELECT r.PROJECT_ID FROM repayment_plan r GROUP BY r.PROJECT_ID",
            " HAVING SUM(CASE WHEN r.REPAYMENT_STATUS = '1' THEN 1 ELSE 0 END) &gt; 0",
            " AND SUM(CASE WHEN r.REPAYMENT_STATUS = '0' THEN 1 ELSE 0 END) &gt; 0)</if>",
            "</where>",
            "ORDER BY p.SHOULD_REPAYMENT_DATE, p.PROJECT_NO, p.NUMBER_OF_PERIODS",
            "</script>"})
    IPage<RepaymentPlanRow> selectPlanPage(Page<RepaymentPlanRow> page, @Param("q") RepaymentPlanQuery query);

    /**
     * 查询某借款人的还款计划
     *
     * @param consumerId 借款人用户标识
     * @param projectId  标的标识, 为空查全部借款
     * @return
     */
    @Select({"<script>", SELECT_PLAN_ROW,
            "WHERE p.CONSUMER_ID = #{consumerId}",
            "<if test='projectId != null'>AND p.PROJECT_ID = #{projectId}</if>",
            "ORDER BY p.PROJECT_ID, p.NUMBER_OF_PERIODS",
            "</script>"})
    List<RepaymentPlanRow> selectConsumerPlans(@Param("consumerId") Long consumerId, @Param("projectId") Long projectId);
}
