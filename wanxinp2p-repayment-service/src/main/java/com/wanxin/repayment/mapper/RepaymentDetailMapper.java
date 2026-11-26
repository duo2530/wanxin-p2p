package com.wanxin.repayment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wanxin.repayment.entity.RepaymentDetail;
import com.wanxin.repayment.model.RepaymentRecordRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author yuelimin
 * @version 1.0.0
 * @since 1.8
 */
@Repository
public interface RepaymentDetailMapper extends BaseMapper<RepaymentDetail> {
    /**
     * 分页查询某借款人已完成(明细已同步)的还款, 按还款时间倒序
     *
     * @param page       分页
     * @param consumerId 借款人用户标识
     * @return
     */
    @Select("SELECT p.PROJECT_ID AS projectId, p.PROJECT_NO AS projectNo, p.NUMBER_OF_PERIODS AS numberOfPeriods,"
            + " d.AMOUNT AS amount, d.REPAYMENT_DATE AS repaymentDate"
            + " FROM repayment_detail d INNER JOIN repayment_plan p ON p.ID = d.REPAYMENT_PLAN_ID"
            + " WHERE d.STATUS = 1 AND p.CONSUMER_ID = #{consumerId}"
            + " ORDER BY d.REPAYMENT_DATE DESC, p.NUMBER_OF_PERIODS DESC")
    IPage<RepaymentRecordRow> selectConsumerRecordPage(Page<RepaymentRecordRow> page, @Param("consumerId") Long consumerId);
}
