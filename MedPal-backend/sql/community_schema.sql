USE medpal;

CREATE TABLE IF NOT EXISTS community_post (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '发布用户ID',
  title VARCHAR(256) NOT NULL COMMENT '帖子标题',
  content TEXT NOT NULL COMMENT '帖子正文',
  image_urls TEXT NULL COMMENT '图片地址，逗号分隔',
  status VARCHAR(32) NOT NULL DEFAULT 'published' COMMENT '状态：published/hidden',
  comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_status (status),
  KEY idx_create_time (create_time),
  CONSTRAINT fk_community_post_user FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT='社区帖子表' COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS community_comment (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '评论用户ID',
  parent_id BIGINT NULL COMMENT '父评论ID',
  content TEXT NOT NULL COMMENT '评论内容',
  image_urls TEXT NULL COMMENT '评论图片地址，逗号分隔',
  status VARCHAR(32) NOT NULL DEFAULT 'published' COMMENT '状态：published/hidden',
  like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (id),
  KEY idx_post_id (post_id),
  KEY idx_user_id (user_id),
  KEY idx_parent_id (parent_id),
  CONSTRAINT fk_community_comment_post FOREIGN KEY (post_id) REFERENCES community_post(id),
  CONSTRAINT fk_community_comment_user FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT='社区评论表' COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS community_like (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  target_type VARCHAR(32) NOT NULL COMMENT '目标类型：post/comment/companion',
  target_id BIGINT NOT NULL COMMENT '目标ID',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  is_delete TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_target (user_id, target_type, target_id),
  KEY idx_target (target_type, target_id),
  CONSTRAINT fk_community_like_user FOREIGN KEY (user_id) REFERENCES user(id)
) COMMENT='社区点赞表' COLLATE=utf8mb4_unicode_ci;
