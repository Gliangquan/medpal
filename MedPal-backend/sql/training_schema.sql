USE medpal;

CREATE TABLE IF NOT EXISTS training_course (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
  title VARCHAR(256) NOT NULL COMMENT '课程标题',
  category VARCHAR(64) NULL COMMENT '分类',
  description VARCHAR(512) NULL COMMENT '课程简介',
  content TEXT NULL COMMENT '课程内容',
  difficulty INT NULL COMMENT '难度 1-5',
  duration INT NULL COMMENT '预计学习时长（分钟）',
  instructor VARCHAR(128) NULL COMMENT '讲师',
  status INT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (id),
  KEY idx_category (category),
  KEY idx_status (status)
) COMMENT='培训课程表' COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS training_record (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '学习记录ID',
  companion_id BIGINT NOT NULL COMMENT '陪诊员ID',
  course_id BIGINT NOT NULL COMMENT '课程ID',
  status VARCHAR(32) NOT NULL DEFAULT 'learning' COMMENT '状态 learning/completed',
  progress INT NOT NULL DEFAULT 0 COMMENT '进度 0-100',
  learning_time INT NOT NULL DEFAULT 0 COMMENT '学习时长（分钟）',
  start_time DATETIME NULL COMMENT '开始学习时间',
  complete_time DATETIME NULL COMMENT '完成时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_companion_course (companion_id, course_id),
  KEY idx_companion_id (companion_id),
  KEY idx_course_id (course_id)
) COMMENT='培训学习记录表' COLLATE=utf8mb4_unicode_ci;
