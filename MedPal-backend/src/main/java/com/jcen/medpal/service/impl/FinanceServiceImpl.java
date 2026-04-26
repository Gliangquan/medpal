package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.AppointmentOrderMapper;
import com.jcen.medpal.mapper.LegacyCompanionMapper;
import com.jcen.medpal.mapper.SettlementMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.Settlement;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.FinanceService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class FinanceServiceImpl extends ServiceImpl<SettlementMapper, Settlement> implements FinanceService {

    @Resource
    private SettlementMapper settlementMapper;

    @Resource
    private AppointmentOrderMapper appointmentOrderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private LegacyCompanionMapper legacyCompanionMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter SETTLEMENT_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public boolean processSettlement(Long id) {
        if (id == null || id == 0) {
            return false;
        }
        if (!settlementTableExists()) {
            createSettlementTableIfMissing();
        }
        if (id < 0) {
            Settlement fallback = buildSettlementByCompanionId(-id);
            if (fallback == null) {
                return false;
            }
            fallback.setStatus("settled");
            fallback.setSettlementTime(LocalDateTime.now());
            fallback.setSettlementNo(generateSettlementNo(fallback.getCompanionId()));
            fallback.setCreateTime(LocalDateTime.now());
            fallback.setUpdateTime(LocalDateTime.now());
            return settlementMapper.insert(fallback) > 0;
        }
        Settlement settlement = settlementMapper.selectById(id);
        if (settlement == null) {
            settlement = buildSettlementByFallbackId(id);
            if (settlement == null) {
                return false;
            }
            settlement.setStatus("settled");
            settlement.setSettlementTime(LocalDateTime.now());
            settlement.setSettlementNo(generateSettlementNo(settlement.getCompanionId()));
            settlement.setCreateTime(LocalDateTime.now());
            settlement.setUpdateTime(LocalDateTime.now());
            return settlementMapper.insert(settlement) > 0;
        }
        settlement.setStatus("settled");
        settlement.setSettlementTime(LocalDateTime.now());
        settlement.setSettlementNo(generateSettlementNo(settlement.getCompanionId()));
        settlement.setUpdateTime(LocalDateTime.now());
        return settlementMapper.updateById(settlement) > 0;
    }

    @Override
    public boolean batchProcessSettlements(String ids) {
        if (StringUtils.isBlank(ids)) {
            return false;
        }
        String[] idArray = ids.split(",");
        boolean processed = false;
        for (String id : idArray) {
            if (StringUtils.isBlank(id)) {
                continue;
            }
            processed = processSettlement(Long.parseLong(id.trim())) || processed;
        }
        return processed;
    }

    @Override
    public Object getFinanceReport(String startDate, String endDate) {
        Map<String, Object> summary = buildFinanceSummary();
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);
        return summary;
    }

    @Override
    public Object getFinanceLogs(long current, long size) {
        if (!settlementTableExists()) {
            return new ArrayList<>();
        }
        Page<Settlement> page = new Page<>(current, size);
        return this.lambdaQuery().orderByDesc(Settlement::getCreateTime).page(page);
    }

    public Map<String, Object> getFinanceSummary() {
        return buildFinanceSummary();
    }

    public Map<String, Object> listSettlementOverview(long current, long size, String status, String keyword) {
        List<Map<String, Object>> rows = buildSettlementRows();
        List<Map<String, Object>> filtered = rows.stream()
                .filter(item -> StringUtils.isBlank(status) || status.equals(item.get("status")))
                .filter(item -> {
                    if (StringUtils.isBlank(keyword)) {
                        return true;
                    }
                    String lower = keyword.toLowerCase(Locale.ROOT);
                    return String.valueOf(item.get("companionName")).toLowerCase(Locale.ROOT).contains(lower)
                            || String.valueOf(item.get("phone")).contains(keyword);
                })
                .collect(Collectors.toList());
        int fromIndex = (int) Math.max(0, (current - 1) * size);
        int toIndex = Math.min(filtered.size(), fromIndex + (int) size);
        List<Map<String, Object>> pageRecords = fromIndex >= filtered.size() ? new ArrayList<>() : filtered.subList(fromIndex, toIndex);
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageRecords);
        result.put("total", filtered.size());
        result.put("current", current);
        result.put("size", size);
        return result;
    }

    private Map<String, Object> buildFinanceSummary() {
        List<Map<String, Object>> settlementRows = buildSettlementRows();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal settledAmount = BigDecimal.ZERO;
        BigDecimal platformFee = BigDecimal.ZERO;

        for (Map<String, Object> row : settlementRows) {
            BigDecimal amount = castBigDecimal(row.get("amount"));
            BigDecimal fee = castBigDecimal(row.get("fee"));
            totalIncome = totalIncome.add(amount);
            platformFee = platformFee.add(fee);
            if ("settled".equals(row.get("status"))) {
                settledAmount = settledAmount.add(amount);
            } else {
                pendingAmount = pendingAmount.add(amount);
            }
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP));
        summary.put("pendingAmount", pendingAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("settledAmount", settledAmount.setScale(2, RoundingMode.HALF_UP));
        summary.put("platformFee", platformFee.setScale(2, RoundingMode.HALF_UP));
        return summary;
    }

    private List<Map<String, Object>> buildSettlementRows() {
        List<AppointmentOrder> completedOrders = appointmentOrderMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<AppointmentOrder>()
                .eq("is_delete", 0)
                .eq("payment_status", "paid")
                .eq("order_status", "completed")
                .isNotNull("companion_id")
                .orderByDesc("update_time"));
        if (completedOrders.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, List<AppointmentOrder>> orderMap = completedOrders.stream().collect(Collectors.groupingBy(AppointmentOrder::getCompanionId, LinkedHashMap::new, Collectors.toList()));
        Map<Long, Settlement> settlementMap = loadSettlementsByCompanion(orderMap.keySet());
        Map<Long, User> userMap = userMapper.selectBatchIds(orderMap.keySet()).stream().collect(Collectors.toMap(User::getId, item -> item));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<AppointmentOrder>> entry : orderMap.entrySet()) {
            Long companionId = entry.getKey();
            List<AppointmentOrder> orders = entry.getValue();
            BigDecimal amount = BigDecimal.ZERO;
            BigDecimal fee = BigDecimal.ZERO;
            int totalHours = 0;
            for (AppointmentOrder order : orders) {
                amount = amount.add(calculateIncome(order));
                fee = fee.add(nullToZero(order.getPlatformFee()));
                totalHours += parseDurationHours(order.getDuration());
            }
            Settlement settlement = settlementMap.get(companionId);
            User companion = userMap.get(companionId);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", settlement != null ? settlement.getId() : -companionId);
            item.put("companionId", companionId);
            item.put("companionName", companion == null ? fallbackCompanionName(companionId) : companion.getUserName());
            item.put("phone", companion == null ? fallbackCompanionPhone(companionId) : companion.getUserPhone());
            item.put("orderCount", orders.size());
            item.put("totalHours", totalHours);
            item.put("amount", amount.setScale(2, RoundingMode.HALF_UP));
            item.put("fee", fee.setScale(2, RoundingMode.HALF_UP));
            item.put("actualAmount", amount.subtract(fee).setScale(2, RoundingMode.HALF_UP));
            item.put("status", settlement == null ? "pending" : settlement.getStatus());
            item.put("settlementTime", settlement == null ? null : settlement.getSettlementTime());
            item.put("settlementNo", settlement == null ? null : settlement.getSettlementNo());
            result.add(item);
        }
        return result;
    }

    private Map<Long, Settlement> loadSettlementsByCompanion(Set<Long> companionIds) {
        if (!settlementTableExists() || companionIds == null || companionIds.isEmpty()) {
            return new HashMap<>();
        }
        return settlementMapper.selectList(new QueryWrapper<Settlement>().in("companion_id", companionIds))
                .stream()
                .collect(Collectors.toMap(Settlement::getCompanionId, item -> item, (a, b) -> {
                    LocalDateTime aTime = a.getUpdateTime() == null ? LocalDateTime.MIN : a.getUpdateTime();
                    LocalDateTime bTime = b.getUpdateTime() == null ? LocalDateTime.MIN : b.getUpdateTime();
                    return aTime.isAfter(bTime) ? a : b;
                }));
    }

    private boolean settlementTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'settlement'",
                Integer.class);
        return count != null && count > 0;
    }

    private void createSettlementTableIfMissing() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS settlement (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id'," +
                "companion_id BIGINT NOT NULL COMMENT '陪诊员ID'," +
                "amount DECIMAL(10,2) NOT NULL COMMENT '结算金额'," +
                "status VARCHAR(32) NOT NULL DEFAULT 'pending' COMMENT '结算状态：pending/settled'," +
                "settlement_time DATETIME NULL COMMENT '结算时间'," +
                "settlement_no VARCHAR(64) NULL COMMENT '结算单号'," +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "UNIQUE KEY uk_companion_id (companion_id)" +
                ") COMMENT='结算表' COLLATE=utf8mb4_unicode_ci");
    }

    private Settlement buildSettlementByFallbackId(Long id) {
        List<Map<String, Object>> rows = buildSettlementRows();
        Map<String, Object> matched = rows.stream()
                .filter(item -> String.valueOf(item.get("id")).equals(String.valueOf(id)))
                .findFirst()
                .orElse(null);
        return matched == null ? null : buildSettlementFromRow(matched);
    }

    private Settlement buildSettlementByCompanionId(Long companionId) {
        List<Map<String, Object>> rows = buildSettlementRows();
        Map<String, Object> matched = rows.stream()
                .filter(item -> String.valueOf(item.get("companionId")).equals(String.valueOf(companionId)))
                .findFirst()
                .orElse(null);
        return matched == null ? null : buildSettlementFromRow(matched);
    }

    private Settlement buildSettlementFromRow(Map<String, Object> matched) {
        Settlement settlement = new Settlement();
        settlement.setCompanionId(Long.parseLong(String.valueOf(matched.get("companionId"))));
        settlement.setAmount(castBigDecimal(matched.get("amount")));
        settlement.setStatus("settled");
        return settlement;
    }

    private String generateSettlementNo(Long companionId) {
        return "SET" + LocalDateTime.now().format(SETTLEMENT_NO_FORMATTER) + String.format("%04d", companionId == null ? 0 : companionId % 10000);
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

    private BigDecimal castBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }

    private int parseDurationHours(String duration) {
        if (StringUtils.isBlank(duration)) {
            return 0;
        }
        String normalized = duration.trim().toLowerCase(Locale.ROOT);
        if (normalized.endsWith("h")) {
            try {
                return (int) Math.round(Double.parseDouble(normalized.replace("h", "")));
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        if ("half_day".equals(normalized)) {
            return 4;
        }
        if ("full_day".equals(normalized)) {
            return 8;
        }
        return 0;
    }

    private String fallbackCompanionName(Long companionId) {
        Map<String, Object> basic = legacyCompanionMapper.selectBasicById(companionId);
        return basic == null ? "陪诊员" : String.valueOf(basic.get("companionName"));
    }

    private String fallbackCompanionPhone(Long companionId) {
        Map<String, Object> basic = legacyCompanionMapper.selectBasicById(companionId);
        return basic == null ? "-" : String.valueOf(basic.get("companionPhone"));
    }
}
