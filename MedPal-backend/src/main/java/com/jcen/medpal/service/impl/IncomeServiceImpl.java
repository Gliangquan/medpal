package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.medpal.mapper.AppointmentOrderMapper;
import com.jcen.medpal.mapper.DepartmentMapper;
import com.jcen.medpal.mapper.HospitalMapper;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.Department;
import com.jcen.medpal.model.entity.Hospital;
import com.jcen.medpal.service.IncomeService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class IncomeServiceImpl implements IncomeService {

    @Resource
    private AppointmentOrderMapper appointmentOrderMapper;

    @Resource
    private HospitalMapper hospitalMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Object getIncomeDetail(Long companionId, String startDate, String endDate) {
        List<AppointmentOrder> orders = listIncomeOrders(companionId, startDate, endDate);
        Map<Long, String> hospitalNameMap = buildHospitalNameMap(orders);
        Map<Long, String> departmentNameMap = buildDepartmentNameMap(orders);
        Map<Long, Map<String, Object>> settlementMap = loadSettlementInfo(companionId);

        List<Map<String, Object>> details = new ArrayList<>();
        for (AppointmentOrder order : orders) {
            Map<String, Object> item = new LinkedHashMap<>();
            BigDecimal incomeAmount = calculateIncome(order);
            Map<String, Object> settlement = settlementMap.get(companionId);
            item.put("orderId", order.getId());
            item.put("orderNo", order.getOrderNo());
            item.put("appointmentDate", order.getAppointmentDate());
            item.put("hospitalName", hospitalNameMap.getOrDefault(order.getHospitalId(), "未填写医院"));
            item.put("departmentName", departmentNameMap.getOrDefault(order.getDepartmentId(), "未填写科室"));
            item.put("orderStatus", order.getOrderStatus());
            item.put("orderStatusText", getOrderStatusText(order.getOrderStatus()));
            item.put("incomeAmount", incomeAmount);
            item.put("serviceFee", nullToZero(order.getServiceFee()));
            item.put("extraFee", nullToZero(order.getExtraFee()));
            item.put("platformFee", nullToZero(order.getPlatformFee()));
            item.put("paymentStatus", order.getPaymentStatus());
            item.put("settlementStatus", settlementStatus(order, settlement));
            item.put("settlementStatusText", settlementStatusText(order, settlement));
            item.put("settlementTime", settlement == null ? null : settlement.get("settlementTime"));
            item.put("settlementNo", settlement == null ? null : settlement.get("settlementNo"));
            item.put("createTime", order.getCreateTime());
            details.add(item);
        }
        return details;
    }

    @Override
    public Object getIncomeStatistics(Long companionId, String type) {
        LocalDateTime start = resolveStatisticsStart(type);
        String startDate = start == null ? null : start.toLocalDate().format(DATE_FORMATTER);
        List<AppointmentOrder> orders = listIncomeOrders(companionId, startDate, null);
        Map<Long, Map<String, Object>> settlementMap = loadSettlementInfo(companionId);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal settledIncome = BigDecimal.ZERO;
        BigDecimal pendingSettlementIncome = BigDecimal.ZERO;
        BigDecimal inServiceIncome = BigDecimal.ZERO;
        int settledCount = 0;
        int pendingSettlementCount = 0;
        int inServiceCount = 0;

        for (AppointmentOrder order : orders) {
            BigDecimal income = calculateIncome(order);
            totalIncome = totalIncome.add(income);
            Map<String, Object> settlement = settlementMap.get(companionId);
            String settlementStatus = settlementStatus(order, settlement);
            if ("settled".equals(settlementStatus)) {
                settledIncome = settledIncome.add(income);
                settledCount++;
            } else if ("pending_settlement".equals(settlementStatus)) {
                pendingSettlementIncome = pendingSettlementIncome.add(income);
                pendingSettlementCount++;
            } else if ("in_service".equals(settlementStatus)) {
                inServiceIncome = inServiceIncome.add(income);
                inServiceCount++;
            }
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("companionId", companionId);
        statistics.put("type", type);
        statistics.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP));
        statistics.put("settledIncome", settledIncome.setScale(2, RoundingMode.HALF_UP));
        statistics.put("pendingSettlementIncome", pendingSettlementIncome.setScale(2, RoundingMode.HALF_UP));
        statistics.put("inServiceIncome", inServiceIncome.setScale(2, RoundingMode.HALF_UP));
        statistics.put("settledCount", settledCount);
        statistics.put("pendingSettlementCount", pendingSettlementCount);
        statistics.put("inServiceCount", inServiceCount);
        statistics.put("orderCount", orders.size());
        return statistics;
    }

    @Override
    public Object getTotalIncome(Long companionId) {
        List<AppointmentOrder> orders = listIncomeOrders(companionId, null, null);
        Map<Long, Map<String, Object>> settlementMap = loadSettlementInfo(companionId);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal settledIncome = BigDecimal.ZERO;
        BigDecimal pendingSettlementIncome = BigDecimal.ZERO;
        for (AppointmentOrder order : orders) {
            BigDecimal income = calculateIncome(order);
            totalIncome = totalIncome.add(income);
            Map<String, Object> settlement = settlementMap.get(companionId);
            String settlementStatus = settlementStatus(order, settlement);
            if ("settled".equals(settlementStatus)) {
                settledIncome = settledIncome.add(income);
            } else if ("pending_settlement".equals(settlementStatus)) {
                pendingSettlementIncome = pendingSettlementIncome.add(income);
            }
        }
        Map<String, Object> total = new HashMap<>();
        total.put("companionId", companionId);
        total.put("amount", totalIncome.setScale(2, RoundingMode.HALF_UP));
        total.put("settledIncome", settledIncome.setScale(2, RoundingMode.HALF_UP));
        total.put("pendingSettlementIncome", pendingSettlementIncome.setScale(2, RoundingMode.HALF_UP));
        total.put("orderCount", orders.size());
        return total;
    }

    @Override
    public Object getSettlementRule() {
        Map<String, Object> rule = new HashMap<>();
        rule.put("settlementCycle", "月结");
        rule.put("serviceFeeRate", 0.1);
        rule.put("description", "已完成且已支付订单计入可结算收入，未录入结算单时统一显示为待结算。");
        return rule;
    }

    private List<AppointmentOrder> listIncomeOrders(Long companionId, String startDate, String endDate) {
        QueryWrapper<AppointmentOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("companion_id", companionId)
                .eq("is_delete", 0)
                .eq("payment_status", "paid")
                .in("order_status", "confirmed", "serving", "completion_pending", "completed")
                .orderByDesc("appointment_date")
                .orderByDesc("create_time");
        LocalDateTime start = parseDateTimeStart(startDate);
        LocalDateTime end = parseDateTimeEnd(endDate);
        if (start != null) {
            wrapper.ge("appointment_date", start);
        }
        if (end != null) {
            wrapper.le("appointment_date", end);
        }
        return appointmentOrderMapper.selectList(wrapper);
    }

    private Map<Long, String> buildHospitalNameMap(List<AppointmentOrder> orders) {
        List<Long> hospitalIds = orders.stream()
                .map(AppointmentOrder::getHospitalId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (hospitalIds.isEmpty()) {
            return new HashMap<>();
        }
        return hospitalMapper.selectBatchIds(hospitalIds).stream()
                .collect(Collectors.toMap(Hospital::getId, item -> item.getHospitalName() == null ? "未填写医院" : item.getHospitalName()));
    }

    private Map<Long, String> buildDepartmentNameMap(List<AppointmentOrder> orders) {
        List<Long> departmentIds = orders.stream()
                .map(AppointmentOrder::getDepartmentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (departmentIds.isEmpty()) {
            return new HashMap<>();
        }
        return departmentMapper.selectBatchIds(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, item -> item.getDepartmentName() == null ? "未填写科室" : item.getDepartmentName()));
    }

    private Map<Long, Map<String, Object>> loadSettlementInfo(Long companionId) {
        Map<Long, Map<String, Object>> result = new HashMap<>();
        if (!settlementTableExists()) {
            return result;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT companion_id AS companionId, amount, status, settlement_time AS settlementTime, settlement_no AS settlementNo FROM settlement WHERE companion_id = ?",
                companionId);
        for (Map<String, Object> row : rows) {
            Object companionIdValue = row.get("companionId");
            if (companionIdValue instanceof Number) {
                result.put(((Number) companionIdValue).longValue(), row);
            }
        }
        return result;
    }

    private boolean settlementTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'settlement'",
                Integer.class);
        return count != null && count > 0;
    }

    private LocalDateTime resolveStatisticsStart(String type) {
        if (type == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        switch (type) {
            case "day":
                return LocalDateTime.of(today, LocalTime.MIN);
            case "week":
                return LocalDateTime.of(today.minusDays(6), LocalTime.MIN);
            case "month":
                return LocalDateTime.of(today.minusDays(29), LocalTime.MIN);
            default:
                return null;
        }
    }

    private LocalDateTime parseDateTimeStart(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(date.trim(), DATE_FORMATTER).atStartOfDay();
    }

    private LocalDateTime parseDateTimeEnd(String date) {
        if (date == null || date.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(date.trim(), DATE_FORMATTER).atTime(LocalTime.MAX);
    }

    private BigDecimal calculateIncome(AppointmentOrder order) {
        BigDecimal serviceFee = nullToZero(order.getServiceFee());
        BigDecimal extraFee = nullToZero(order.getExtraFee());
        BigDecimal platformFee = nullToZero(order.getPlatformFee());
        BigDecimal totalPrice = nullToZero(order.getTotalPrice());
        BigDecimal income = serviceFee.add(extraFee);
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            income = totalPrice.subtract(platformFee);
        }
        if (income.compareTo(BigDecimal.ZERO) < 0) {
            income = BigDecimal.ZERO;
        }
        return income.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String settlementStatus(AppointmentOrder order, Map<String, Object> settlement) {
        if (settlement != null && "settled".equals(String.valueOf(settlement.get("status")))) {
            return "settled";
        }
        if ("completed".equals(order.getOrderStatus())) {
            return "pending_settlement";
        }
        return "in_service";
    }

    private String settlementStatusText(AppointmentOrder order, Map<String, Object> settlement) {
        String status = settlementStatus(order, settlement);
        if ("settled".equals(status)) {
            return "已结算";
        }
        if ("pending_settlement".equals(status)) {
            return "待结算";
        }
        return "服务中";
    }

    private String getOrderStatusText(String status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case "confirmed":
                return "已接单";
            case "serving":
                return "服务中";
            case "completion_pending":
                return "待用户确认";
            case "completed":
                return "已完成";
            default:
                return status;
        }
    }
}
