package com.jcen.medpal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcen.medpal.model.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 支付记录Mapper
 *
 * @author <a href="https://github.com/Gliangquan">小梁</a>
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * 根据订单ID查询支付记录
     *
     * @param orderId 订单ID
     * @return 支付记录
     */
    @Select("SELECT * FROM payment WHERE order_id = #{orderId} AND is_delete = 0 ORDER BY create_time DESC LIMIT 1")
    Payment selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据支付单号查询支付记录
     *
     * @param paymentNo 支付单号
     * @return 支付记录
     */
    @Select("SELECT * FROM payment WHERE payment_no = #{paymentNo} AND is_delete = 0")
    Payment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    /**
     * 查询订单的所有支付记录
     *
     * @param orderId 订单ID
     * @return 支付记录列表
     */
    @Select("SELECT * FROM payment WHERE order_id = #{orderId} AND is_delete = 0 ORDER BY create_time DESC")
    List<Payment> selectListByOrderId(@Param("orderId") Long orderId);
}
