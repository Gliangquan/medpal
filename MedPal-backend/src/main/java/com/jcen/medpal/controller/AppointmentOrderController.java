package com.jcen.medpal.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.OrderDetailVO;
import org.apache.commons.lang3.StringUtils;
import com.jcen.medpal.service.AppointmentOrderService;
import com.jcen.medpal.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/order")
public class AppointmentOrderController {
    
    @Resource
    private AppointmentOrderService appointmentOrderService;

    @Resource
    private UserService userService;
    
    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Object createOrder(@RequestBody AppointmentOrder order, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            if (!"patient".equals(loginUser.getUserRole()) && !"user".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅患者可发单");
            }
            order.setUserId(loginUser.getId());
            AppointmentOrder result = appointmentOrderService.createOrder(order);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建订单失败");
        }
    }

    /**
     * 保存订单草稿
     */
    @PostMapping("/draft/save")
    public Object saveDraft(@RequestBody AppointmentOrder order, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            if (!"patient".equals(loginUser.getUserRole()) && !"user".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅患者可保存草稿");
            }
            if (order.getId() != null) {
                AppointmentOrder existing = appointmentOrderService.getById(order.getId());
                if (existing == null || existing.getUserId() == null || !existing.getUserId().equals(loginUser.getId())) {
                    return ResultUtils.error(40300, "无权更新该草稿");
                }
            }
            order.setUserId(loginUser.getId());
            AppointmentOrder result = appointmentOrderService.saveDraft(order);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "保存草稿失败");
        }
    }

    /**
     * 获取用户草稿列表
     */
    @GetMapping("/draft/list")
    public Object listDrafts(@RequestParam(defaultValue = "1") long current,
                            @RequestParam(defaultValue = "10") long size,
                            HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            if (!"patient".equals(loginUser.getUserRole()) && !"user".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅患者可查看草稿");
            }
            Page<AppointmentOrder> page = new Page<>(current, size);
            IPage<AppointmentOrder> result = appointmentOrderService.lambdaQuery()
                    .eq(AppointmentOrder::getUserId, loginUser.getId())
                    .eq(AppointmentOrder::getOrderStatus, "draft")
                    .orderByDesc(AppointmentOrder::getUpdateTime)
                    .orderByDesc(AppointmentOrder::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    /**
     * 提交草稿为正式订单
     */
    @PostMapping("/draft/submit/{id}")
    public Object submitDraft(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            AppointmentOrder order = appointmentOrderService.getById(id);
            if (order == null || !"draft".equals(order.getOrderStatus())) {
                return ResultUtils.error(40000, "草稿不存在或状态错误");
            }
            if (order.getUserId() == null || !order.getUserId().equals(loginUser.getId())) {
                return ResultUtils.error(40300, "无权提交该草稿");
            }
            if (order.getHospitalId() == null || order.getDepartmentId() == null || order.getAppointmentDate() == null
                    || StringUtils.isBlank(order.getDuration())) {
                return ResultUtils.error(40000, "草稿信息不完整，无法提交");
            }
            order.setOrderStatus("pending");
            order.setOrderNo(appointmentOrderService.generateOrderNo());
            order.setUpdateTime(LocalDateTime.now());
            boolean result = appointmentOrderService.updateById(order);
            return result ? ResultUtils.success(order) : ResultUtils.error(40000, "提交失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "提交失败");
        }
    }
    
    /**
     * 获取用户订单列表
     */
    @GetMapping("/list")
    public Object listOrders(@RequestParam(defaultValue = "1") long current,
                            @RequestParam(defaultValue = "10") long size,
                            @RequestParam(required = false) Long userId,
                            @RequestParam(required = false) String orderStatus,
                            @RequestParam(required = false) String keyword,
                            HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            Long queryUserId = userId;
            if (!isAdmin && ("patient".equals(loginUser.getUserRole()) || "user".equals(loginUser.getUserRole()))) {
                queryUserId = loginUser.getId();
            } else if (!isAdmin && userId != null && !userId.equals(loginUser.getId())) {
                return ResultUtils.error(40300, "无权限查看该用户订单");
            }
            Page<AppointmentOrder> page = new Page<>(current, size);
            String keywordTrimmed = StringUtils.trimToNull(keyword);
            IPage<AppointmentOrder> result = appointmentOrderService.lambdaQuery()
                    .eq(queryUserId != null, AppointmentOrder::getUserId, queryUserId)
                    .eq(orderStatus != null, AppointmentOrder::getOrderStatus, orderStatus)
                    .and(keywordTrimmed != null, wrapper -> wrapper.like(AppointmentOrder::getOrderNo, keywordTrimmed))
                    .orderByDesc(AppointmentOrder::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询订单失败");
        }
    }
    
    /**
     * 获取陪诊员订单列表
     */
    @GetMapping("/list-by-companion")
    public Object listOrdersByCompanion(@RequestParam(defaultValue = "1") long current,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam Long companionId,
                                       @RequestParam(required = false) String orderStatus,
                                       HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            if (!isAdmin && !"companion".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅陪诊员可查看该列表");
            }
            if (!isAdmin && !companionId.equals(loginUser.getId())) {
                return ResultUtils.error(40300, "无权限查看他人服务订单");
            }
            Page<AppointmentOrder> page = new Page<>(current, size);
            IPage<AppointmentOrder> result = appointmentOrderService.lambdaQuery()
                    .eq(AppointmentOrder::getCompanionId, companionId)
                    .eq(orderStatus != null, AppointmentOrder::getOrderStatus, orderStatus)
                    .orderByDesc(AppointmentOrder::getCreateTime)
                    .page(page);
            return ResultUtils.success(result);
        } catch (BusinessException e) {
            return ResultUtils.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询订单失败");
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Object getOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            OrderDetailVO orderDetail = appointmentOrderService.getOrderDetail(id);
            if (orderDetail == null) {
                return ResultUtils.error(40400, "订单不存在");
            }
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean isPatientOwner = orderDetail.getUserId() != null && orderDetail.getUserId().equals(loginUser.getId());
            boolean isCompanionOwner = orderDetail.getCompanionId() != null && orderDetail.getCompanionId().equals(loginUser.getId());
            boolean isCompanionViewingMarket = "companion".equals(loginUser.getUserRole())
                    && orderDetail.getCompanionId() == null
                    && ("pending".equals(orderDetail.getOrderStatus()) || "confirmed".equals(orderDetail.getOrderStatus()));
            if (!isAdmin && !isPatientOwner && !isCompanionOwner && !isCompanionViewingMarket) {
                return ResultUtils.error(40300, "无权限查看该订单");
            }
            if (isCompanionViewingMarket) {
                orderDetail.setPatientPhone(null);
                orderDetail.setSpecificNeeds(maskContactPhone(orderDetail.getSpecificNeeds()));
            }
            return ResultUtils.success(orderDetail);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询订单失败");
        }
    }
    
    /**
     * 接单
     */
    @PostMapping("/accept/{id}")
    public Object acceptOrder(@PathVariable Long id, @RequestParam Long companionId, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            if (!"companion".equals(loginUser.getUserRole())) {
                return ResultUtils.error(40300, "仅陪诊员可接单");
            }
            if (!companionId.equals(loginUser.getId())) {
                return ResultUtils.error(40300, "无权限代他人接单");
            }
            if (!"approved".equals(loginUser.getRealNameStatus()) || !"approved".equals(loginUser.getQualificationStatus())) {
                return ResultUtils.error(40300, "请先完成实名认证与资质认证");
            }
            AppointmentOrder order = appointmentOrderService.getById(id);
            if (order == null) {
                return ResultUtils.error(40400, "订单不存在");
            }
            boolean isPending = "pending".equals(order.getOrderStatus());
            boolean isLegacyUnassignedConfirmed = "confirmed".equals(order.getOrderStatus()) && order.getCompanionId() == null;
            if (!isPending && !isLegacyUnassignedConfirmed) {
                return ResultUtils.error(40000, "当前订单状态不可接单");
            }
            if (!"paid".equals(order.getPaymentStatus())) {
                return ResultUtils.error(40000, "订单未支付，暂不可接单");
            }
            if (order.getCompanionId() != null && !order.getCompanionId().equals(companionId)) {
                return ResultUtils.error(40300, "该订单已指定其他陪诊员");
            }
            boolean result = appointmentOrderService.acceptOrder(id, companionId);
            return result ? ResultUtils.success("接单成功") : ResultUtils.error(40000, "接单失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "接单失败");
        }
    }
    
    /**
     * 拒单
     */
    @PostMapping("/reject/{id}")
    public Object rejectOrder(@PathVariable Long id, @RequestParam String reason) {
        try {
            boolean result = appointmentOrderService.rejectOrder(id, reason);
            return result ? ResultUtils.success("拒单成功") : ResultUtils.error(40000, "拒单失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "拒单失败");
        }
    }
    
    /**
     * 完成订单
     */
    @PostMapping("/complete/{id}")
    public Object completeOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            AppointmentOrder order = appointmentOrderService.getById(id);
            if (order == null) {
                return ResultUtils.error(40400, "订单不存在");
            }
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean isCompanionOwner = order.getCompanionId() != null && order.getCompanionId().equals(loginUser.getId());
            if (!isAdmin && !isCompanionOwner) {
                return ResultUtils.error(40300, "仅陪诊员可发起完成申请");
            }
            boolean result = appointmentOrderService.completeOrder(id, loginUser.getId(), isAdmin);
            return result ? ResultUtils.success("已提交完成申请，等待用户确认") : ResultUtils.error(40000, "仅已接单且已支付订单可发起完成");
        } catch (Exception e) {
            return ResultUtils.error(40000, "完成订单失败");
        }
    }

    /**
     * 用户确认完成订单
     */
    @PostMapping("/confirm-complete/{id}")
    public Object confirmCompleteOrder(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            AppointmentOrder order = appointmentOrderService.getById(id);
            if (order == null) {
                return ResultUtils.error(40400, "订单不存在");
            }
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean isPatientOwner = order.getUserId() != null && order.getUserId().equals(loginUser.getId());
            if (!isAdmin && !isPatientOwner) {
                return ResultUtils.error(40300, "仅患者可确认完成");
            }
            boolean result = appointmentOrderService.confirmCompleted(id, loginUser.getId(), isAdmin);
            return result ? ResultUtils.success("订单已完成") : ResultUtils.error(40000, "当前订单状态不可确认完成");
        } catch (Exception e) {
            return ResultUtils.error(40000, "确认完成失败");
        }
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel/{id}")
    public Object cancelOrder(@PathVariable Long id, @RequestParam String reason, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            AppointmentOrder order = appointmentOrderService.getById(id);
            if (order == null) {
                return ResultUtils.error(40400, "订单不存在");
            }
            if (!"admin".equals(loginUser.getUserRole())
                    && (order.getUserId() == null || !order.getUserId().equals(loginUser.getId()))) {
                return ResultUtils.error(40300, "仅患者本人可取消订单");
            }
            if ("confirmed".equals(order.getOrderStatus())) {
                return ResultUtils.error(40000, "已接单订单不可取消");
            }
            boolean isAdmin = "admin".equals(loginUser.getUserRole());
            boolean result = appointmentOrderService.cancelOrder(id, reason, loginUser.getId(), isAdmin);
            return result ? ResultUtils.success("取消订单成功") : ResultUtils.error(40000, "仅待接单订单可取消");
        } catch (Exception e) {
            return ResultUtils.error(40000, "取消订单失败");
        }
    }
    
    /**
     * 更新订单状态
     */
    @PostMapping("/update-status/{id}")
    public Object updateOrderStatus(@PathVariable Long id, @RequestParam String orderStatus) {
        try {
            AppointmentOrder order = new AppointmentOrder();
            order.setId(id);
            order.setOrderStatus(orderStatus);
            boolean result = appointmentOrderService.updateById(order);
            return result ? ResultUtils.success("更新成功") : ResultUtils.error(40000, "更新失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "更新失败");
        }
    }

    private String maskContactPhone(String specificNeeds) {
        if (StringUtils.isBlank(specificNeeds)) {
            return specificNeeds;
        }
        return specificNeeds.replaceAll("联系手机号：[^；\\s]+", "联系手机号：平台内联系");
    }
}
