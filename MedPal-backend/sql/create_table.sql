# MedPal 陪诊服务系统 - 数据库初始化脚本（优化版）

-- 创建库
create database if not exists medpal;
use medpal;

-- ==================== 用户相关表 ====================

-- 用户表（患者/用户）
drop table if exists user;
create table user(
    id bigint auto_increment comment 'id' primary key,
    user_account varchar(256) not null unique comment '账号',
    user_password varchar(512) not null comment '密码',
    user_name varchar(256) null comment '用户昵称',
    user_avatar varchar(1024) null comment '用户头像',
    user_profile varchar(512) null comment '用户简介',
    user_role varchar(256) default 'user' not null comment '用户角色：user/admin/ban',
    user_phone varchar(20) null unique comment '手机号',
    user_email varchar(256) null unique comment '邮箱',
    status tinyint default 1 not null comment '账户状态：1启用 0禁用',
    union_id varchar(256) null unique comment '开放平台id（微信 UnionID）',
    mp_open_id varchar(256) null unique comment '小程序openId',
    create_by varchar(256) null comment '创建人',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_by varchar(256) null comment '更新人',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_user_phone (user_phone),
    index idx_user_account (user_account),
    index idx_create_time (create_time)
) comment '用户表' collate = utf8mb4_unicode_ci;

-- 陪诊员表
drop table if exists companion;
create table companion(
    id bigint auto_increment comment 'id' primary key,
    companion_account varchar(256) not null unique comment '账号',
    companion_password varchar(512) not null comment '密码',
    companion_name varchar(256) not null comment '姓名',
    companion_avatar varchar(1024) null comment '头像',
    companion_phone varchar(20) not null unique comment '手机号',
    id_card varchar(20) not null unique comment '身份证号',
    id_card_front varchar(1024) null comment '身份证正面',
    id_card_back varchar(1024) null comment '身份证反面',
    qualification_cert varchar(1024) null comment '资质证书',
    qualification_type varchar(256) null comment '资质类型：nurse/caregiver/other',
    work_years int null comment '从业年限',
    specialties text null comment '擅长领域',
    service_area varchar(256) null comment '服务区域',
    profile_photo varchar(1024) null comment '个人免冠照',
    real_name_status varchar(20) default 'pending' comment '实名认证状态：pending/approved/rejected',
    qualification_status varchar(20) default 'pending' comment '资质认证状态：pending/approved/rejected',
    rating decimal(3,2) default 0 comment '平均评分',
    service_count int default 0 comment '服务次数',
    total_income decimal(10,2) default 0 comment '总收入',
    status tinyint default 1 not null comment '账户状态：1启用 0禁用',
    mp_open_id varchar(256) null unique comment '小程序openId',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_companion_phone (companion_phone),
    index idx_rating (rating),
    index idx_create_time (create_time)
) comment '陪诊员表' collate = utf8mb4_unicode_ci;

-- ==================== 医疗资源表 ====================

-- 医院表
drop table if exists hospital;
create table hospital(
    id bigint auto_increment comment 'id' primary key,
    hospital_name varchar(256) not null comment '医院名称',
    hospital_level varchar(50) null comment '医院等级：三甲/三乙/二甲/二乙/一级',
    address varchar(512) not null comment '医院地址',
    latitude decimal(10,8) null comment '纬度',
    longitude decimal(11,8) null comment '经度',
    phone varchar(20) null comment '医院电话',
    introduction text null comment '医院介绍',
    status tinyint default 1 not null comment '状态：1启用 0禁用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_hospital_name (hospital_name),
    index idx_status (status)
) comment '医院表' collate = utf8mb4_unicode_ci;

-- 科室表
drop table if exists department;
create table department(
    id bigint auto_increment comment 'id' primary key,
    hospital_id bigint not null comment '医院id',
    department_name varchar(256) not null comment '科室名称',
    department_code varchar(50) null comment '科室代码',
    introduction text null comment '科室介绍',
    status tinyint default 1 not null comment '状态：1启用 0禁用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    foreign key (hospital_id) references hospital(id),
    index idx_hospital_id (hospital_id),
    index idx_department_name (department_name)
) comment '科室表' collate = utf8mb4_unicode_ci;

-- 医生表
drop table if exists doctor;
create table doctor(
    id bigint auto_increment comment 'id' primary key,
    hospital_id bigint not null comment '医院id',
    department_id bigint not null comment '科室id',
    doctor_name varchar(256) not null comment '医生姓名',
    doctor_title varchar(100) null comment '职称：主任医师/副主任医师/主治医师/医师',
    specialties text null comment '擅长领域',
    introduction text null comment '医生介绍',
    clinic_time varchar(256) null comment '出诊时间',
    status tinyint default 1 not null comment '状态：1启用 0禁用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    foreign key (hospital_id) references hospital(id),
    foreign key (department_id) references department(id),
    index idx_hospital_id (hospital_id),
    index idx_department_id (department_id),
    index idx_doctor_name (doctor_name)
) comment '医生表' collate = utf8mb4_unicode_ci;

-- ==================== 订单相关表 ====================

-- 订单表
drop table if exists appointment_order;
create table appointment_order(
    id bigint auto_increment comment 'id' primary key,
    order_no varchar(50) not null unique comment '订单编号',
    user_id bigint not null comment '用户id',
    companion_id bigint null comment '陪诊员id',
    hospital_id bigint not null comment '医院id',
    department_id bigint not null comment '科室id',
    doctor_id bigint null comment '医生id',
    appointment_date datetime not null comment '就诊时间',
    duration varchar(50) not null comment '陪诊时长：2h/half_day/full_day',
    specific_needs text null comment '具体需求',
    order_status varchar(50) default 'pending' comment '订单状态：pending/confirmed/serving/completed/cancelled',
    total_price decimal(10,2) not null comment '总价格',
    service_fee decimal(10,2) null comment '陪诊服务费',
    extra_fee decimal(10,2) default 0 comment '额外服务费',
    platform_fee decimal(10,2) null comment '平台服务费',
    payment_status varchar(50) default 'unpaid' comment '支付状态：unpaid/paid/refunded',
    cancel_reason varchar(512) null comment '取消原因',
    cancel_time datetime null comment '取消时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    foreign key (user_id) references user(id),
    foreign key (companion_id) references companion(id),
    foreign key (hospital_id) references hospital(id),
    foreign key (department_id) references department(id),
    foreign key (doctor_id) references doctor(id),
    index idx_user_id (user_id),
    index idx_companion_id (companion_id),
    index idx_order_status (order_status),
    index idx_create_time (create_time),
    index idx_appointment_date (appointment_date)
) comment '订单表' collate = utf8mb4_unicode_ci;

-- ==================== 评价相关表 ====================

-- 评价表
drop table if exists evaluation;
create table evaluation(
    id bigint auto_increment comment 'id' primary key,
    order_id bigint not null comment '订单id',
    user_id bigint not null comment '用户id',
    companion_id bigint not null comment '陪诊员id',
    professionalism_score int null comment '专业度评分：1-5',
    attitude_score int null comment '态度评分：1-5',
    efficiency_score int null comment '效率评分：1-5',
    satisfaction_score int null comment '满意度评分：1-5',
    average_score decimal(3,2) null comment '平均评分',
    evaluation_text text null comment '文字评价',
    evaluation_images varchar(2048) null comment '评价图片（JSON数组）',
    status varchar(50) default 'pending' comment '状态：pending/published/hidden',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    foreign key (order_id) references appointment_order(id),
    foreign key (user_id) references user(id),
    foreign key (companion_id) references companion(id),
    index idx_order_id (order_id),
    index idx_companion_id (companion_id),
    index idx_average_score (average_score)
) comment '评价表' collate = utf8mb4_unicode_ci;

-- ==================== 服务记录表 ====================

-- 服务记录表
drop table if exists service_record;
create table service_record(
    id bigint auto_increment comment 'id' primary key,
    order_id bigint not null comment '订单id',
    companion_id bigint not null comment '陪诊员id',
    user_id bigint not null comment '用户id',
    service_process text null comment '就诊流程',
    doctor_advice text null comment '医生建议',
    user_condition_change text null comment '用户病情变化',
    service_summary text null comment '服务总结',
    actual_duration int null comment '实际服务时长（分钟）',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    foreign key (order_id) references appointment_order(id),
    foreign key (companion_id) references companion(id),
    foreign key (user_id) references user(id),
    index idx_order_id (order_id),
    index idx_companion_id (companion_id)
) comment '服务记录表' collate = utf8mb4_unicode_ci;

-- ==================== 内容管理表 ====================

-- 平台内容表（科普文章 / 平台公告）
drop table if exists content;
create table content(
    id bigint auto_increment comment 'id' primary key,
    title varchar(256) not null comment '标题',
    type varchar(50) not null comment '类型：article-科普文章, announcement-平台公告',
    content text not null comment '正文内容（支持 Markdown）',
    summary varchar(512) null comment '摘要',
    cover_image varchar(1024) null comment '封面图',
    tags varchar(512) null comment '标签或扩展信息（例如 targets:users,companions）',
    priority int default 0 comment '优先级（公告可用）',
    status varchar(32) default 'draft' comment '状态：draft/published/unpublished',
    view_count int default 0 comment '阅读量',
    publish_time datetime null comment '发布时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_type (type),
    index idx_status (status),
    index idx_publish_time (publish_time),
    index idx_create_time (create_time)
) comment '平台内容表' collate = utf8mb4_unicode_ci;

-- ==================== 消息相关表 ====================

-- 消息通知表
drop table if exists notification;
create table notification(
    id bigint auto_increment comment 'id' primary key,
    user_id bigint not null comment '用户id',
    type varchar(50) not null default 'system' comment '消息类型：order-订单, system-系统, activity-活动',
    title varchar(256) not null comment '消息标题',
    content text not null comment '消息内容',
    related_type varchar(50) null comment '关联类型：order-订单',
    related_id bigint null comment '关联ID',
    status varchar(20) default 'unread' comment '状态：unread-未读, read-已读',
    read_time datetime null comment '阅读时间',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_user_id (user_id),
    index idx_status (status),
    index idx_type (type),
    index idx_create_time (create_time)
) comment '消息通知表' collate = utf8mb4_unicode_ci;

-- 聊天记录表
drop table if exists chat_message;
create table chat_message(
    id bigint auto_increment comment 'id' primary key,
    order_id bigint not null comment '订单id',
    sender_id bigint not null comment '发送人id',
    sender_type varchar(50) not null comment '发送人类型：user/companion',
    receiver_id bigint not null comment '接收人id',
    message_type varchar(50) default 'text' comment '消息类型：text/image',
    content text not null comment '消息内容',
    image_url varchar(1024) null comment '图片url',
    is_read tinyint default 0 comment '是否已读',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    is_delete tinyint default 0 not null comment '是否删除',
    foreign key (order_id) references appointment_order(id),
    index idx_order_id (order_id),
    index idx_sender_id (sender_id),
    index idx_receiver_id (receiver_id),
    index idx_create_time (create_time)
) comment '聊天记录表' collate = utf8mb4_unicode_ci;

-- ==================== 支付相关表 ====================

-- 支付记录表
drop table if exists payment;
create table payment(
    id bigint auto_increment comment '支付ID' primary key,
    payment_no varchar(64) not null unique comment '支付单号',
    order_id bigint not null comment '订单ID',
    order_no varchar(64) not null comment '订单编号',
    user_id bigint not null comment '用户ID',
    amount decimal(10,2) not null comment '支付金额',
    payment_channel varchar(32) default 'mock' comment '支付渠道：mock/alipay/wechat',
    payment_status varchar(32) default 'unpaid' comment '支付状态：unpaid/processing/paid/failed',
    transaction_id varchar(128) null comment '第三方支付流水号',
    paid_time datetime null comment '支付时间',
    expire_time datetime null comment '过期时间',
    voucher_no varchar(64) null comment '支付凭证号',
    description varchar(512) null comment '支付描述',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete tinyint default 0 not null comment '是否删除',
    index idx_order_id (order_id),
    index idx_user_id (user_id),
    index idx_payment_status (payment_status),
    index idx_create_time (create_time)
) comment '支付记录表' collate = utf8mb4_unicode_ci;

-- ==================== 系统配置表 ====================

-- 价格配置表
drop table if exists price_config;
create table price_config(
    id bigint auto_increment comment 'id' primary key,
    duration varchar(50) not null unique comment '时长：2h/half_day/full_day',
    base_price decimal(10,2) not null comment '基础价格',
    platform_fee_rate decimal(5,2) not null comment '平台手续费比例（%）',
    status tinyint default 1 not null comment '状态：1启用 0禁用',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '价格配置表' collate = utf8mb4_unicode_ci;

-- 系统设置表
drop table if exists system_config;
create table system_config(
    id bigint auto_increment comment 'id' primary key,
    config_key varchar(256) not null unique comment '配置键',
    config_value text not null comment '配置值',
    config_type varchar(50) null comment '配置类型',
    description varchar(512) null comment '描述',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_config_key (config_key)
) comment '系统设置表' collate = utf8mb4_unicode_ci;

-- 用户设置表
drop table if exists user_settings;
create table user_settings(
    id bigint auto_increment comment 'id' primary key,
    user_id bigint not null comment '用户id',
    order_notify tinyint default 1 comment '订单通知：1启用 0禁用',
    activity_notify tinyint default 1 comment '活动通知：1启用 0禁用',
    system_notify tinyint default 1 comment '系统通知：1启用 0禁用',
    medical_record_visibility varchar(50) default 'private' comment '病历可见性：private/companion/public',
    profile_visibility varchar(50) default 'private' comment '个人资料可见性：private/companion/public',
    chat_history_retention int default 30 comment '聊天记录保存时长（天）',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    unique key uk_user_id (user_id),
    foreign key (user_id) references user(id),
    index idx_user_id (user_id)
) comment '用户设置表' collate = utf8mb4_unicode_ci;

-- ==================== 初始化数据 ====================

-- 插入价格配置
insert into price_config (duration, base_price, platform_fee_rate, status) values
('2h', 100.00, 10, 1),
('half_day', 200.00, 10, 1),
('full_day', 350.00, 10, 1);

-- 插入系统配置
insert into system_config (config_key, config_value, config_type, description) values
('app_name', 'MedPal', 'system', '应用名称'),
('app_version', '1.0.0', 'system', '应用版本'),
('service_phone', '400-800-8888', 'contact', '客服电话'),
('service_email', 'service@medpal.com', 'contact', '客服邮箱');

-- 创建管理员账号（如果不存在）
insert ignore into user (user_account, user_password, user_name, user_role, status, create_time) values
('admin', '8d969eef6ecad3c29a3a873fba8f4f7f', 'Administrator', 'admin', 1, NOW());
