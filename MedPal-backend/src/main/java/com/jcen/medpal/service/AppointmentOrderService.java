package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.vo.OrderDetailVO;

public interface AppointmentOrderService extends IService<AppointmentOrder> {

    String generateOrderNo();

    AppointmentOrder createOrder(AppointmentOrder order);

    boolean acceptOrder(Long orderId, Long companionId);

    boolean rejectOrder(Long orderId, String reason);

    boolean completeOrder(Long orderId, Long operatorUserId, boolean isAdmin);

    boolean confirmCompleted(Long orderId, Long operatorUserId, boolean isAdmin);

    boolean cancelOrder(Long orderId, String reason, Long operatorUserId, boolean isAdmin);

    AppointmentOrder saveDraft(AppointmentOrder order);

    /**
     * 获取订单详情（包含关联信息）
     */
    OrderDetailVO getOrderDetail(Long orderId);
}
