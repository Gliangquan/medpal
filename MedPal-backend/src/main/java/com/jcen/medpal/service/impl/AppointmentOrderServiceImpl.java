package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.AppointmentOrderMapper;
import com.jcen.medpal.mapper.DepartmentMapper;
import com.jcen.medpal.mapper.DoctorMapper;
import com.jcen.medpal.mapper.HospitalMapper;
import com.jcen.medpal.mapper.LegacyCompanionMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.entity.AppointmentOrder;
import com.jcen.medpal.model.entity.Department;
import com.jcen.medpal.model.entity.Doctor;
import com.jcen.medpal.model.entity.Hospital;
import com.jcen.medpal.model.entity.MedicalRecord;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.OrderDetailVO;
import com.jcen.medpal.service.AppointmentOrderService;
import com.jcen.medpal.service.EvaluationService;
import com.jcen.medpal.service.MedicalRecordService;
import com.jcen.medpal.service.NotificationService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AppointmentOrderServiceImpl extends ServiceImpl<AppointmentOrderMapper, AppointmentOrder> implements AppointmentOrderService {

    @Autowired
    private HospitalMapper hospitalMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LegacyCompanionMapper legacyCompanionMapper;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private NotificationService notificationService;
    
    @Override
    public String generateOrderNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        int random = (int)(Math.random() * 10000);
        return "#" + timestamp + String.format("%04d", random);
    }
    
    @Override
    public AppointmentOrder createOrder(AppointmentOrder order) {
        order.setOrderNo(generateOrderNo());
        order.setOrderStatus("pending");
        order.setPaymentStatus("unpaid");
        order.setCreateTime(LocalDateTime.now());
        this.save(order);
        return order;
    }
    
    @Override
    public boolean acceptOrder(Long orderId, Long companionId) {
        AppointmentOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        boolean isPending = "pending".equals(order.getOrderStatus());
        boolean isLegacyUnassignedConfirmed = "confirmed".equals(order.getOrderStatus()) && order.getCompanionId() == null;
        if (!isPending && !isLegacyUnassignedConfirmed) {
            return false;
        }
        if (!"paid".equals(order.getPaymentStatus())) {
            return false;
        }
        if (order.getCompanionId() != null && !order.getCompanionId().equals(companionId)) {
            return false;
        }
        if (order.getCompanionId() == null) {
            order.setCompanionId(companionId);
        }
        order.setOrderStatus("confirmed");
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }
    
    @Override
    public boolean rejectOrder(Long orderId, String reason) {
        AppointmentOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        order.setOrderStatus("pending");
        order.setCompanionId(null);
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeOrder(Long orderId, Long operatorUserId, boolean isAdmin) {
        AppointmentOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        boolean isConfirmed = "confirmed".equals(order.getOrderStatus());
        boolean isLegacyServing = "serving".equals(order.getOrderStatus());
        if (!isConfirmed && !isLegacyServing) {
            return false;
        }
        if (!"paid".equals(order.getPaymentStatus())) {
            return false;
        }
        if (order.getCompanionId() == null) {
            return false;
        }
        if (!isAdmin) {
            boolean isCompanionOwner = order.getCompanionId() != null && order.getCompanionId().equals(operatorUserId);
            if (!isCompanionOwner) {
                return false;
            }
        }
        order.setOrderStatus("completion_pending");
        order.setCompletionRequestedTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(order);
        if (updated && order.getUserId() != null) {
            notificationService.createNotification(
                    order.getUserId(),
                    "order",
                    "陪诊员申请完成订单",
                    "您的陪诊订单已由陪诊员申请完成，请确认服务是否结束",
                    "order",
                    order.getId());
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmCompleted(Long orderId, Long operatorUserId, boolean isAdmin) {
        AppointmentOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!"completion_pending".equals(order.getOrderStatus())) {
            return false;
        }
        if (!"paid".equals(order.getPaymentStatus())) {
            return false;
        }
        if (order.getCompanionId() == null) {
            return false;
        }
        if (!isAdmin) {
            boolean isPatientOwner = order.getUserId() != null && order.getUserId().equals(operatorUserId);
            if (!isPatientOwner) {
                return false;
            }
        }
        order.setOrderStatus("completed");
        order.setUpdateTime(LocalDateTime.now());
        boolean updated = this.updateById(order);
        if (!updated) {
            return false;
        }
        boolean syncResult = syncCompanionStatsAfterOrderCompleted(order);
        if (!syncResult) {
            throw new IllegalStateException("订单完成后联动更新失败");
        }
        ensureMedicalRecord(order);
        if (order.getCompanionId() != null) {
            notificationService.createNotification(
                    order.getCompanionId(),
                    "order",
                    "订单已完成",
                    "用户已确认订单完成，可前往查看收入与评价",
                    "order",
                    order.getId());
        }
        return true;
    }
    
    @Override
    public boolean cancelOrder(Long orderId, String reason, Long operatorUserId, boolean isAdmin) {
        AppointmentOrder order = this.getById(orderId);
        if (order == null) {
            return false;
        }
        if (!isAdmin) {
            boolean isPatientOwner = order.getUserId() != null && order.getUserId().equals(operatorUserId);
            if (!isPatientOwner) {
                return false;
            }
        }
        if ("cancelled".equals(order.getOrderStatus()) || "completed".equals(order.getOrderStatus())) {
            return false;
        }
        // 已接单后不允许取消（已支付 + 已接单）
        if ("confirmed".equals(order.getOrderStatus())) {
            return false;
        }
        if (!"pending".equals(order.getOrderStatus())) {
            return false;
        }
        order.setOrderStatus("cancelled");
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return this.updateById(order);
    }

    @Override
    public AppointmentOrder saveDraft(AppointmentOrder order) {
        LocalDateTime now = LocalDateTime.now();
        if (order.getId() != null) {
            AppointmentOrder existing = this.getById(order.getId());
            if (existing != null) {
                existing.setHospitalId(order.getHospitalId());
                existing.setDepartmentId(order.getDepartmentId());
                existing.setDoctorId(order.getDoctorId());
                existing.setCompanionId(order.getCompanionId());
                existing.setAppointmentDate(order.getAppointmentDate());
                existing.setDuration(order.getDuration());
                existing.setSpecificNeeds(order.getSpecificNeeds());
                existing.setTotalPrice(order.getTotalPrice());
                existing.setServiceFee(order.getServiceFee());
                existing.setExtraFee(order.getExtraFee());
                existing.setPlatformFee(order.getPlatformFee());
                existing.setOrderStatus("draft");
                existing.setPaymentStatus("unpaid");
                existing.setUpdateTime(now);
                this.updateById(existing);
                return existing;
            }
        }
        order.setOrderStatus("draft");
        order.setOrderNo(null);
        order.setPaymentStatus("unpaid");
        order.setCreateTime(now);
        order.setUpdateTime(now);
        this.save(order);
        return order;
    }

    @Override
    public OrderDetailVO getOrderDetail(Long orderId) {
        AppointmentOrder order = this.getById(orderId);
        if (order == null) {
            return null;
        }

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);

        // 查询医院名称
        if (order.getHospitalId() != null) {
            Hospital hospital = hospitalMapper.selectById(order.getHospitalId());
            if (hospital != null) {
                vo.setHospitalName(hospital.getHospitalName());
            }
        }

        // 查询科室名称
        if (order.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(order.getDepartmentId());
            if (department != null) {
                vo.setDepartmentName(department.getDepartmentName());
            }
        }

        // 查询医生名称
        if (order.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(order.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getDoctorName());
            }
        }

        if (order.getUserId() != null) {
            User patient = userMapper.selectById(order.getUserId());
            if (patient != null) {
                vo.setPatientPhone(patient.getUserPhone());
            }
        }

        // 查询陪诊员信息
        if (order.getCompanionId() != null) {
            User companion = getCompanionUser(order.getCompanionId());
            if (companion != null) {
                vo.setCompanionName(companion.getUserName());
                vo.setCompanionPhone(companion.getUserPhone());
            } else {
                Map<String, Object> legacyCompanion = legacyCompanionMapper.selectBasicById(order.getCompanionId());
                if (legacyCompanion != null) {
                    vo.setCompanionName((String) legacyCompanion.get("companionName"));
                    vo.setCompanionPhone((String) legacyCompanion.get("companionPhone"));
                }
            }
        }

        return vo;
    }

    private boolean syncCompanionStatsAfterOrderCompleted(AppointmentOrder order) {
        if (order == null || order.getCompanionId() == null) {
            return false;
        }
        User companion = getCompanionUser(order.getCompanionId());
        if (companion == null) {
            return false;
        }

        int currentServiceCount = companion.getServiceCount() == null ? 0 : companion.getServiceCount();
        companion.setServiceCount(currentServiceCount + 1);

        BigDecimal income = calculateCompanionIncome(order);
        double currentIncome = companion.getTotalIncome() == null ? 0D : companion.getTotalIncome();
        BigDecimal totalIncome = BigDecimal.valueOf(currentIncome).add(income).setScale(2, RoundingMode.HALF_UP);
        companion.setTotalIncome(totalIncome.doubleValue());

        Double latestRating = evaluationService.getCompanionAverageRating(companion.getId());
        if (latestRating != null && latestRating > 0) {
            BigDecimal normalizedRating = BigDecimal.valueOf(latestRating).setScale(2, RoundingMode.HALF_UP);
            companion.setRating(normalizedRating.doubleValue());
        }
        companion.setUpdateTime(new Date());
        return userMapper.updateById(companion) > 0;
    }

    private void ensureMedicalRecord(AppointmentOrder order) {
        if (order == null || order.getUserId() == null) {
            return;
        }
        long existingCount = medicalRecordService.lambdaQuery()
                .eq(MedicalRecord::getUserId, order.getUserId())
                .eq(MedicalRecord::getVisitDate, order.getAppointmentDate())
                .eq(MedicalRecord::getHospitalName, getHospitalName(order.getHospitalId()))
                .eq(MedicalRecord::getDepartmentName, getDepartmentName(order.getDepartmentId()))
                .eq(MedicalRecord::getDoctorName, getDoctorName(order.getDoctorId()))
                .count();
        if (existingCount > 0) {
            return;
        }
        MedicalRecord record = new MedicalRecord();
        record.setUserId(order.getUserId());
        record.setHospitalName(getHospitalName(order.getHospitalId()));
        record.setDepartmentName(getDepartmentName(order.getDepartmentId()));
        record.setDoctorName(getDoctorName(order.getDoctorId()));
        record.setVisitDate(order.getAppointmentDate());
        record.setSymptoms(extractSection(order.getSpecificNeeds(), "补充说明："));
        record.setDiagnosis("完成一次陪诊服务，待医生进一步补充诊断");
        record.setTreatment("已完成陪诊、挂号/问诊/检查等基础陪同服务");
        record.setDoctorAdvice("请根据医生建议按时复诊，并妥善保存检查报告与处方信息");
        record.setCheckResults(extractSection(order.getSpecificNeeds(), "额外需求："));
        record.setPrescription("");
        medicalRecordService.createMedicalRecord(record);
    }

    private User getCompanionUser(Long companionId) {
        if (companionId == null) {
            return null;
        }
        User companion = userMapper.selectById(companionId);
        if (companion == null || !"companion".equals(companion.getUserRole())) {
            return null;
        }
        return companion;
    }

    private BigDecimal calculateCompanionIncome(AppointmentOrder order) {
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

    private String getHospitalName(Long hospitalId) {
        if (hospitalId == null) {
            return "未填写医院";
        }
        Hospital hospital = hospitalMapper.selectById(hospitalId);
        return hospital != null && hospital.getHospitalName() != null ? hospital.getHospitalName() : "未填写医院";
    }

    private String getDepartmentName(Long departmentId) {
        if (departmentId == null) {
            return "未填写科室";
        }
        Department department = departmentMapper.selectById(departmentId);
        return department != null && department.getDepartmentName() != null ? department.getDepartmentName() : "未填写科室";
    }

    private String getDoctorName(Long doctorId) {
        if (doctorId == null) {
            return "未填写医生";
        }
        Doctor doctor = doctorMapper.selectById(doctorId);
        return doctor != null && doctor.getDoctorName() != null ? doctor.getDoctorName() : "未填写医生";
    }

    private String extractSection(String text, String prefix) {
        if (text == null || prefix == null) {
            return "";
        }
        String[] parts = text.split("；");
        for (String part : parts) {
            String trimmed = part == null ? "" : part.trim();
            if (trimmed.startsWith(prefix)) {
                return trimmed.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
