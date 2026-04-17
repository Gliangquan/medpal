SET @has_completion_requested_time := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'appointment_order'
    AND COLUMN_NAME = 'completion_requested_time'
);
SET @alter_sql := IF(
  @has_completion_requested_time = 0,
  'ALTER TABLE appointment_order ADD COLUMN completion_requested_time datetime NULL COMMENT ''完成申请时间'' AFTER cancel_time',
  'SELECT ''completion_requested_time exists'''
);
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS medical_record (
    id bigint auto_increment comment 'id' primary key,
    user_id bigint not null comment '用户id',
    record_no varchar(64) not null unique comment '病历编号',
    hospital_name varchar(256) null comment '医院名称',
    department_name varchar(256) null comment '科室名称',
    doctor_name varchar(256) null comment '医生姓名',
    visit_date datetime null comment '就诊日期',
    diagnosis text null comment '诊断结果',
    symptoms text null comment '症状描述',
    treatment text null comment '治疗方案',
    prescription text null comment '处方信息',
    check_results text null comment '检查结果',
    doctor_advice text null comment '医生建议',
    attachments text null comment '附件地址（逗号分隔或 JSON）',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_user_id (user_id),
    index idx_visit_date (visit_date),
    index idx_create_time (create_time)
) comment='电子病历表' collate=utf8mb4_unicode_ci;

INSERT IGNORE INTO medical_record (
  user_id, record_no, hospital_name, department_name, doctor_name, visit_date,
  diagnosis, symptoms, treatment, prescription, check_results, doctor_advice, attachments, create_time
) VALUES
(1, 'MR202502100001', '北京协和医院', '心内科', '王医生', '2025-02-10 09:00:00', '高血压伴心律不齐', '头晕、胸闷、心悸', '建议规律服药并复查心电图', '缬沙坦片，每日一次', '心电图提示窦性心律不齐', '低盐饮食，避免熬夜', '/api/file/preview/medical_record/1/report-ecg-20250210.jpg', '2025-02-10 16:30:00'),
(2, 'MR202502110001', '北京大学第一医院', '妇产科', '陈医生', '2025-02-11 10:00:00', '子宫肌瘤随访', '月经量增多', '建议定期复查彩超', '', '彩超提示肌瘤稳定', '三个月后复诊', '', '2025-02-11 17:10:00'),
(3, 'MR202502120001', '中国人民解放军总医院', '消化科', '周医生', '2025-02-12 14:00:00', '慢性胃炎', '胃胀、反酸', '口服抑酸药物治疗', '奥美拉唑，每日一次', '胃镜提示慢性浅表性胃炎', '清淡饮食，少食多餐', '/api/file/preview/medical_record/3/gastroscopy-20250212.pdf', '2025-02-12 18:00:00');

SHOW TABLES LIKE 'medical_record';
DESC appointment_order;
SELECT id,user_id,record_no,attachments FROM medical_record ORDER BY id;
