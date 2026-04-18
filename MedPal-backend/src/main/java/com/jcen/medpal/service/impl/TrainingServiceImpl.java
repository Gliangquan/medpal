package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.TrainingCourseMapper;
import com.jcen.medpal.mapper.TrainingRecordMapper;
import com.jcen.medpal.model.entity.TrainingCourse;
import com.jcen.medpal.model.entity.TrainingRecord;
import com.jcen.medpal.service.TrainingService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TrainingServiceImpl extends ServiceImpl<TrainingCourseMapper, TrainingCourse> implements TrainingService {
    
    @Resource
    private TrainingRecordMapper trainingRecordMapper;

    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public TrainingCourse getCourseById(Long id) {
        ensureTrainingTables();
        return this.getById(id);
    }
    
    @Override
    public TrainingRecord recordLearning(TrainingRecord record) {
        ensureTrainingTables();
        record.setUpdateTime(LocalDateTime.now());
        if (record.getStartTime() == null) {
            record.setStartTime(LocalDateTime.now());
        }
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        if (record.getProgress() == null) {
            record.setProgress(0);
        }
        if (record.getLearningTime() == null) {
            record.setLearningTime(0);
        }
        if (record.getStatus() == null || record.getStatus().trim().isEmpty()) {
            record.setStatus(record.getProgress() != null && record.getProgress() >= 100 ? "completed" : "learning");
        }
        if ("completed".equals(record.getStatus()) && record.getCompleteTime() == null) {
            record.setCompleteTime(LocalDateTime.now());
            if (record.getProgress() == null || record.getProgress() < 100) {
                record.setProgress(100);
            }
        }
        TrainingRecord existed = trainingRecordMapper.selectOne(new QueryWrapper<TrainingRecord>()
                .eq("companion_id", record.getCompanionId())
                .eq("course_id", record.getCourseId())
                .eq("is_delete", 0)
                .last("LIMIT 1"));
        if (existed != null) {
            record.setId(existed.getId());
            if (record.getCreateTime() == null) {
                record.setCreateTime(existed.getCreateTime());
            }
            trainingRecordMapper.updateById(record);
            return trainingRecordMapper.selectById(existed.getId());
        }
        record.setIsDelete(0);
        trainingRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public List<Object> getLearningRecords(Long companionId, long current, long size) {
        ensureTrainingTables();
        long offset = Math.max(0, (current - 1) * size);
        List<TrainingRecord> records = trainingRecordMapper.selectList(new QueryWrapper<TrainingRecord>()
                .eq("companion_id", companionId)
                .eq("is_delete", 0)
                .orderByDesc("update_time")
                .last("LIMIT " + offset + ", " + size));
        if (records.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> courseIds = records.stream().map(TrainingRecord::getCourseId).collect(Collectors.toSet());
        Map<Long, TrainingCourse> courseMap = this.listByIds(courseIds).stream().collect(Collectors.toMap(TrainingCourse::getId, item -> item));
        return records.stream().map(record -> {
            Map<String, Object> item = new LinkedHashMap<>();
            TrainingCourse course = courseMap.get(record.getCourseId());
            item.put("id", record.getId());
            item.put("courseId", record.getCourseId());
            item.put("courseTitle", course == null ? "课程" : course.getTitle());
            item.put("category", course == null ? null : course.getCategory());
            item.put("instructor", course == null ? null : course.getInstructor());
            item.put("duration", course == null ? null : course.getDuration());
            item.put("status", record.getStatus());
            item.put("progress", record.getProgress());
            item.put("learningTime", record.getLearningTime());
            item.put("startTime", record.getStartTime());
            item.put("completeTime", record.getCompleteTime());
            item.put("updateTime", record.getUpdateTime());
            return item;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Object getLearningProgress(Long companionId, Long courseId) {
        ensureTrainingTables();
        TrainingRecord record = trainingRecordMapper.selectOne(new QueryWrapper<TrainingRecord>()
                .eq("companion_id", companionId)
                .eq("course_id", courseId)
                .eq("is_delete", 0)
                .last("LIMIT 1"));
        Map<String, Object> progress = new HashMap<>();
        progress.put("companionId", companionId);
        progress.put("courseId", courseId);
        progress.put("progress", record == null ? 0 : record.getProgress());
        progress.put("status", record == null ? "not_started" : record.getStatus());
        progress.put("learningTime", record == null ? 0 : record.getLearningTime());
        progress.put("startTime", record == null ? null : record.getStartTime());
        progress.put("completeTime", record == null ? null : record.getCompleteTime());
        return progress;
    }

    private void ensureTrainingTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS training_course (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID'," +
                "title VARCHAR(256) NOT NULL COMMENT '课程标题'," +
                "category VARCHAR(64) NULL COMMENT '分类'," +
                "description VARCHAR(512) NULL COMMENT '课程简介'," +
                "content TEXT NULL COMMENT '课程内容'," +
                "difficulty INT NULL COMMENT '难度 1-5'," +
                "duration INT NULL COMMENT '预计学习时长（分钟）'," +
                "instructor VARCHAR(128) NULL COMMENT '讲师'," +
                "status INT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用'," +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'," +
                "KEY idx_category (category), KEY idx_status (status)" +
                ") COMMENT='培训课程表' COLLATE=utf8mb4_unicode_ci");
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS training_record (" +
                "id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '学习记录ID'," +
                "companion_id BIGINT NOT NULL COMMENT '陪诊员ID'," +
                "course_id BIGINT NOT NULL COMMENT '课程ID'," +
                "status VARCHAR(32) NOT NULL DEFAULT 'learning' COMMENT '状态 learning/completed'," +
                "progress INT NOT NULL DEFAULT 0 COMMENT '进度 0-100'," +
                "learning_time INT NOT NULL DEFAULT 0 COMMENT '学习时长（分钟）'," +
                "start_time DATETIME NULL COMMENT '开始学习时间'," +
                "complete_time DATETIME NULL COMMENT '完成时间'," +
                "create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                "update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'," +
                "is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除'," +
                "UNIQUE KEY uk_companion_course (companion_id, course_id), KEY idx_companion_id (companion_id), KEY idx_course_id (course_id)" +
                ") COMMENT='培训学习记录表' COLLATE=utf8mb4_unicode_ci");
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM training_course", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO training_course (title, category, description, content, difficulty, duration, instructor, status, create_time, update_time, is_delete) VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW(), NOW(), 0)",
                    List.of(
                            new Object[]{"陪诊服务沟通技巧", "沟通技巧", "掌握陪诊前、中、后与患者沟通的关键话术和情绪安抚技巧。", "一、接单前要确认患者需求与就诊信息。\n二、到院后要主动同步流程进度。\n三、服务结束后要清晰转述医生建议并提醒复诊。", 2, 25, "平台培训讲师"},
                            new Object[]{"门诊就医流程与风险提示", "服务技巧", "熟悉挂号、候诊、检查、缴费、取药等流程，避免陪诊过程出现遗漏。", "一、提前确认院区与科室位置。\n二、检查项目需提醒空腹、留尿等注意事项。\n三、重要票据与报告要协助患者妥善保存。", 3, 30, "高级陪诊督导"},
                            new Object[]{"老年患者陪护基础", "医疗知识", "学习老年患者常见就医场景中的安全照护与协助方法。", "一、慢病患者重点关注血压、血糖、跌倒风险。\n二、轮椅与搀扶协助要注意姿势。\n三、与家属保持沟通，同步就医进展。", 3, 35, "护理培训老师"},
                            new Object[]{"儿童就诊协助要点", "医疗知识", "了解儿童就医时的沟通方式、资料准备与常见检查配合技巧。", "一、提前准备既往检查单与疫苗接种信息。\n二、与家长确认过敏史。\n三、陪检过程中要稳定儿童情绪。", 2, 20, "儿科护理讲师"},
                            new Object[]{"突发情况应对与紧急联动", "应急处理", "提升对突发不适、跌倒、情绪异常等场景的识别与处置能力。", "一、先确保患者安全。\n二、必要时启动紧急求助或通知家属。\n三、严重情况第一时间联系急诊或120。", 4, 40, "应急处置讲师"}
                    )
            );
        }
    }
}
