package com.wanxin.repayment.service;

import com.wanxin.api.repayment.model.RepaymentPlanDTO;
import com.wanxin.api.repayment.model.RepaymentRequest;
import com.wanxin.api.transaction.model.ProjectWithTendersDTO;
import com.wanxin.common.domain.PageVO;
import com.wanxin.repayment.entity.RepaymentDetail;
import com.wanxin.repayment.entity.RepaymentPlan;
import com.wanxin.repayment.model.RepaymentExecution;
import com.wanxin.repayment.model.RepaymentPlanQuery;
import com.wanxin.repayment.model.RepaymentPlanVO;
import com.wanxin.repayment.model.RepaymentRecordVO;

import java.util.List;

/**
 * @author yuelimin
 * @version 1.0.0
 * @since 1.8
 */
public interface RepaymentService {
    /**
     * 远程调用确认还款接口
     *
     * @param repaymentPlan
     * @param repaymentRequest
     */
    void invokeConfirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest);

    /**
     * 执行还款
     *
     * @param date yyyy-MM-dd
     * @return 到期条数和已发起确认还款的条数
     */
    RepaymentExecution executeRepayment(String date);

    /**
     * 后台分页查询还款计划
     *
     * @param query    查询条件
     * @param pageNo   页码, 从 1 开始
     * @param pageSize 每页条数
     * @return
     */
    PageVO<RepaymentPlanVO> queryRepaymentPlans(RepaymentPlanQuery query, int pageNo, int pageSize);

    /**
     * 查询借款人的还款计划
     *
     * @param consumerId 借款人用户标识
     * @param projectId  标的标识, 为空查全部借款
     * @return
     */
    List<RepaymentPlanVO> queryConsumerRepaymentPlans(Long consumerId, Long projectId);

    /**
     * 分页查询借款人已完成的还款
     *
     * @param consumerId 借款人用户标识
     * @param pageNo     页码, 从 1 开始
     * @param pageSize   每页条数
     * @return
     */
    PageVO<RepaymentRecordVO> queryConsumerRepaymentRecords(Long consumerId, int pageNo, int pageSize);

    /**
     * 确认还款处理
     *
     * @param repaymentPlan
     * @param repaymentRequest
     * @return
     */
    Boolean confirmRepayment(RepaymentPlan repaymentPlan, RepaymentRequest repaymentRequest);

    /**
     * 还款预处理-冻结借款人应还金额
     *
     * @param repaymentPlan
     * @param preRequestNo
     * @return
     */
    Boolean preRepayment(RepaymentPlan repaymentPlan, String preRequestNo);

    /**
     * 根据还款计划生成还款明细并保存
     *
     * @param repaymentPlan
     * @return
     */
    RepaymentDetail saveRepaymentDetail(RepaymentPlan repaymentPlan);

    /**
     * 查询到期还款计划
     *
     * @param date yyyy-MM-dd
     * @return
     */
    List<RepaymentPlanDTO> selectDueRepayment(String date);

    /**
     * 启动还款
     *
     * @param projectWithTendersDTO
     * @return
     */
    String startRepayment(ProjectWithTendersDTO projectWithTendersDTO);
}
