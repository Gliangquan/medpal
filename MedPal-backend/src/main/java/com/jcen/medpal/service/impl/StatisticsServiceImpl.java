package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jcen.medpal.mapper.AppointmentOrderMapper;
import com.jcen.medpal.mapper.EvaluationMapper;
import com.jcen.medpal.mapper.LegacyCompanionMapper;
import com.jcen.medpal.mapper.PaymentMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.Evaluation;
import com.jcen.medpal.model.entity.Payment;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.StatisticsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private AppointmentOrderMapper appointmentOrderMapper;

    @Resource
    private EvaluationMapper evaluationMapper;

    @Resource
    private PaymentMapper paymentMapper;

    @Resource
    private LegacyCompanionMapper legacyCompanionMapper;

    @Override
    public Object getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        long totalUsers = countUsersByRoles("user", "patient", "companion", "admin");
        long totalCompanions = countRegisteredCompanions();
        long totalOrders = countOrdersBetween(null, null);
        BigDecimal totalRevenue = sumRevenueBetween(null, null);
        double avgRating = getAverageRatingBetween(null, null);

        dashboard.put("totalUsers", totalUsers);
        dashboard.put("totalCompanions", totalCompanions);
        dashboard.put("totalOrders", totalOrders);
        dashboard.put("totalRevenue", totalRevenue);
        dashboard.put("avgRating", avgRating);
        return dashboard;
    }

    @Override
    public Object getUserStatistics(String startDate, String endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Map<String, Object> stats = new HashMap<>();
        stats.put("startDate", start.toString());
        stats.put("endDate", end.toString());
        stats.put("totalUsers", countUsersByRoles("user", "patient", "companion", "admin"));
        stats.put("totalCompanions", countRegisteredCompanions());
        stats.put("totalOrders", countOrdersBetween(start, end));
        stats.put("avgRating", getAverageRatingBetween(start, end));
        stats.put("trend", buildDailyTrend(start, end));
        return stats;
    }

    @Override
    public Object getOrderStatistics(String startDate, String endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Map<String, Object> stats = new HashMap<>();
        stats.put("startDate", start.toString());
        stats.put("endDate", end.toString());
        stats.put("totalOrders", countOrdersBetween(start, end));
        stats.put("completedOrders", countOrdersByStatusBetween("completed", start, end));
        stats.put("cancelledOrders", countOrdersByStatusBetween("cancelled", start, end));
        stats.put("pendingOrders", countOrdersByStatusBetween("pending", start, end));
        stats.put("completionPendingOrders", countOrdersByStatusBetween("completion_pending", start, end));
        return stats;
    }

    @Override
    public Object getCompanionStatistics(String startDate, String endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Map<String, Object> stats = new HashMap<>();
        stats.put("startDate", start.toString());
        stats.put("endDate", end.toString());
        stats.put("totalCompanions", countRegisteredCompanions());
        stats.put("approvedCompanions", countApprovedCompanions());
        stats.put("activeCompanions", countActiveCompanionsBetween(start, end));
        stats.put("averageRating", getAverageRatingBetween(start, end));
        return stats;
    }

    @Override
    public Object getRevenueStatistics(String startDate, String endDate) {
        LocalDate[] range = normalizeRange(startDate, endDate);
        LocalDate start = range[0];
        LocalDate end = range[1];
        Map<String, Object> stats = new HashMap<>();
        stats.put("startDate", start.toString());
        stats.put("endDate", end.toString());
        stats.put("totalRevenue", sumRevenueBetween(start, end));
        stats.put("paidOrders", countPaidOrdersBetween(start, end));
        stats.put("refundOrders", countPaymentStatusBetween("refunded", start, end));
        return stats;
    }

    private LocalDate[] normalizeRange(String startDate, String endDate) {
        LocalDate end = parseDate(endDate, LocalDate.now());
        LocalDate start = parseDate(startDate, end.minusDays(29));
        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }
        return new LocalDate[] { start, end };
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private long countUsersByRoles(String... roles) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).in("user_role", (Object[]) roles);
        Long count = userMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countRegisteredCompanions() {
        long userCompanionCount = countUsersByRoles("companion");
        if (userCompanionCount > 0) {
            return userCompanionCount;
        }
        Long legacyCount = legacyCompanionMapper.countAllCompanions();
        return legacyCount == null ? 0L : legacyCount;
    }

    private long countApprovedCompanions() {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0)
                .eq("user_role", "companion")
                .eq("real_name_status", "approved")
                .eq("qualification_status", "approved");
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            return count;
        }
        Long legacyCount = legacyCompanionMapper.countApprovedCompanions();
        return legacyCount == null ? 0L : legacyCount;
    }

    private long countOrdersBetween(LocalDate start, LocalDate end) {
        QueryWrapper<AppointmentOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0);
        applyRange(wrapper, start, end, "create_time");
        Long count = appointmentOrderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countOrdersByStatusBetween(String status, LocalDate start, LocalDate end) {
        QueryWrapper<AppointmentOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).eq("order_status", status);
        applyRange(wrapper, start, end, "create_time");
        Long count = appointmentOrderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countPaidOrdersBetween(LocalDate start, LocalDate end) {
        QueryWrapper<AppointmentOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).eq("payment_status", "paid");
        applyRange(wrapper, start, end, "create_time");
        Long count = appointmentOrderMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countPaymentStatusBetween(String paymentStatus, LocalDate start, LocalDate end) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).eq("payment_status", paymentStatus);
        applyRange(wrapper, start, end, "create_time");
        Long count = paymentMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private long countActiveCompanionsBetween(LocalDate start, LocalDate end) {
        QueryWrapper<AppointmentOrder> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).isNotNull("companion_id");
        applyRange(wrapper, start, end, "create_time");
        List<AppointmentOrder> orders = appointmentOrderMapper.selectList(wrapper);
        return orders.stream()
                .map(AppointmentOrder::getCompanionId)
                .filter(id -> id != null)
                .distinct()
                .count();
    }

    private BigDecimal sumRevenueBetween(LocalDate start, LocalDate end) {
        QueryWrapper<Payment> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).eq("payment_status", "paid");
        applyRange(wrapper, start, end, "paid_time");
        List<Payment> payments = paymentMapper.selectList(wrapper);
        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : payments) {
            if (payment.getAmount() != null) {
                total = total.add(payment.getAmount());
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private double getAverageRatingBetween(LocalDate start, LocalDate end) {
        QueryWrapper<Evaluation> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0).eq("status", "published");
        applyRange(wrapper, start, end, "create_time");
        List<Evaluation> evaluations = evaluationMapper.selectList(wrapper);
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (Evaluation evaluation : evaluations) {
            if (evaluation.getAverageScore() != null) {
                total = total.add(evaluation.getAverageScore());
                count++;
            }
        }
        if (count == 0) {
            return 0D;
        }
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP).doubleValue();
    }

    private List<Map<String, Object>> buildDailyTrend(LocalDate start, LocalDate end) {
        List<Map<String, Object>> trend = new ArrayList<>();
        long days = ChronoUnit.DAYS.between(start, end);
        for (int i = 0; i <= days; i++) {
            LocalDate current = start.plusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("date", current.toString());
            item.put("newOrders", countOrdersBetween(current, current));
            item.put("completedOrders", countOrdersByStatusBetween("completed", current, current));
            item.put("newUsers", countUsersCreatedBetween(current, current));
            item.put("dailyIncome", sumRevenueBetween(current, current));
            item.put("avgRating", getAverageRatingBetween(current, current));
            trend.add(item);
        }
        return trend;
    }

    private long countUsersCreatedBetween(LocalDate start, LocalDate end) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0);
        applyRange(wrapper, start, end, "create_time");
        Long count = userMapper.selectCount(wrapper);
        return count == null ? 0L : count;
    }

    private void applyRange(QueryWrapper<?> wrapper, LocalDate start, LocalDate end, String column) {
        if (start != null) {
            wrapper.ge(column, start.atStartOfDay());
        }
        if (end != null) {
            wrapper.lt(column, end.plusDays(1).atStartOfDay());
        }
    }
}
