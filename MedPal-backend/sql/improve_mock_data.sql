-- MedPal 数据库测试数据改进脚本
-- 用于补充和完善现有的测试数据

USE medpal;

-- ==================== 1. 订单表改进 ====================
-- 添加已取消订单
INSERT INTO appointment_order (order_no, user_id, companion_id, hospital_id, department_id, doctor_id, appointment_date, duration, specific_needs, order_status, total_price, service_fee, extra_fee, platform_fee, payment_status, cancel_reason, cancel_time, create_time) VALUES
('ORD20250301001', 1, 11, 4, 10, 11, '2025-03-05 09:00:00', '2h', '用户临时有事', 'cancelled', 110.00, 100.00, 0.00, 10.00, 'unpaid', '用户临时有事，需要改期', '2025-02-28 18:00:00', '2025-02-28 17:00:00'),
('ORD20250302001', 2, 12, 5, 13, 14, '2025-03-06 10:00:00', 'half_day', '陪诊员无法接单', 'cancelled', 220.00, 200.00, 0.00, 20.00, 'unpaid', '陪诊员因故无法接单', '2025-02-28 19:00:00', '2025-02-28 18:30:00');

-- ==================== 2. 聊天记录表改进 ====================
-- 添加更多聊天记录
INSERT INTO chat_message (order_id, sender_id, sender_type, receiver_id, message_type, content, is_read, create_time) VALUES
-- 订单1的更多对话
(1, 1, 'user', 1, 'text', '王医生，我的血压一直在140/90左右，需要调整用药吗？', 0, '2025-02-10 09:40:00'),
(1, 1, 'companion', 1, 'text', '用户您好，这个血压确实需要关注，建议咨询医生是否需要调整用药方案', 1, '2025-02-10 09:45:00'),
(1, 1, 'user', 1, 'text', '好的，谢谢你的建议', 0, '2025-02-10 09:50:00'),
(1, 1, 'companion', 1, 'text', '不客气，我会全程陪同您，有任何问题随时告诉我', 1, '2025-02-10 09:55:00'),

-- 订单2的更多对话
(2, 2, 'user', 2, 'text', '李护士，检查前需要空腹吗？', 0, '2025-02-11 10:20:00'),
(2, 2, 'companion', 2, 'text', '用户您好，这个检查需要空腹8小时以上，请注意', 1, '2025-02-11 10:25:00'),
(2, 2, 'user', 2, 'text', '明白了，我已经空腹了', 0, '2025-02-11 10:30:00'),
(2, 2, 'companion', 2, 'text', '很好，那我们现在就去做检查吧', 1, '2025-02-11 10:35:00'),

-- 订单3的更多对话
(3, 3, 'user', 3, 'text', '张陪诊员，我的腿不太好，走路有点困难', 0, '2025-02-12 14:40:00'),
(3, 3, 'companion', 3, 'text', '用户您好，我已经为您准备好轮椅，我会全程推您，请放心', 1, '2025-02-12 14:45:00'),
(3, 3, 'user', 3, 'text', '谢谢你，你真贴心', 0, '2025-02-12 14:50:00'),
(3, 3, 'companion', 3, 'text', '这是我的职责，让我们一起完成今天的就医', 1, '2025-02-12 14:55:00');

-- ==================== 3. 平台内容表改进 ====================
-- 添加更多科普文章
INSERT INTO content (title, type, content, summary, tags, priority, status, view_count, publish_time, create_time) VALUES
('冬季常见疾病预防指南', 'article', '# 冬季常见疾病预防指南\n\n## 流感预防\n- 接种流感疫苗\n- 避免接触患者\n- 保持手卫生\n\n## 心脑血管疾病\n- 保暖很重要\n- 避免过度劳累\n- 定期监测血压\n\n## 呼吸道疾病\n- 增强体质\n- 避免烟雾环境\n- 及时就医', '冬季是多种疾病的高发季节，做好预防工作很重要', '冬季,预防,疾病', 0, 'published', 45, '2025-02-20 10:00:00', '2025-02-19 18:00:00'),
('老年人就医陪诊注意事项', 'article', '# 老年人就医陪诊注意事项\n\n## 出门前准备\n- 带好医保卡和身份证\n- 准备好近期检查报告\n- 穿着舒适的衣服\n\n## 就医过程中\n- 全程陪同，避免走散\n- 帮助记录医生建议\n- 协助办理各项手续\n\n## 就医后\n- 帮助取药\n- 记录用药方法\n- 定期复诊提醒', '为老年患者提供专业的陪诊服务，确保就医顺利', '老年人,陪诊,就医', 0, 'published', 38, '2025-02-21 11:00:00', '2025-02-20 19:30:00'),
('如何与医生有效沟通', 'article', '# 如何与医生有效沟通\n\n## 准备工作\n- 列出主要症状\n- 记录发病时间\n- 准备既往病史\n\n## 沟通技巧\n- 清楚描述症状\n- 主动提问\n- 记录医生建议\n\n## 常见问题\n- 用药方法\n- 复诊时间\n- 日常护理', '有效的医患沟通能提高就医效率和治疗效果', '沟通,医患,就医', 0, 'published', 52, '2025-02-22 09:30:00', '2025-02-21 17:00:00'),
('常见检查项目解读', 'article', '# 常见检查项目解读\n\n## 血液检查\n- 血常规：检查血细胞数量\n- 生化检查：检查肝肾功能\n- 血糖检查：检查血糖水平\n\n## 影像检查\n- X光：检查骨骼和肺部\n- 超声：检查腹部器官\n- CT：详细检查内部结构\n\n## 心电图\n- 检查心脏电活动\n- 诊断心律不齐\n- 评估心脏功能', '了解常见检查项目，更好地配合医生诊疗', '检查,诊断,医学', 0, 'published', 67, '2025-02-23 14:00:00', '2025-02-22 20:00:00'),
('康复期间的自我护理', 'article', '# 康复期间的自我护理\n\n## 饮食调理\n- 清淡易消化\n- 营养均衡\n- 避免刺激性食物\n\n## 活动安排\n- 循序渐进\n- 避免过度劳累\n- 定期复诊\n\n## 心理调适\n- 保持乐观心态\n- 家人陪伴支持\n- 必要时心理咨询', '康复期间的正确护理能加快恢复进程', '康复,护理,健康', 0, 'published', 41, '2025-02-24 10:30:00', '2025-02-23 16:00:00'),
('医保报销常见问题解答', 'article', '# 医保报销常见问题解答\n\n## 报销比例\n- 门诊报销：50-70%\n- 住院报销：70-90%\n- 特殊病种：更高比例\n\n## 报销流程\n- 持医保卡就医\n- 自动结算\n- 定期查询\n\n## 常见问题\n- 异地就医\n- 自费项目\n- 报销上限', '了解医保政策，合理利用医保资源', '医保,报销,政策', 0, 'published', 58, '2025-02-25 13:00:00', '2025-02-24 18:30:00'),
('春季养生保健建议', 'article', '# 春季养生保健建议\n\n## 起居调适\n- 早睡早起\n- 适应季节变化\n- 避免过度疲劳\n\n## 饮食调理\n- 清淡营养\n- 春笋、春菜\n- 避免过油腻\n\n## 运动保健\n- 适度运动\n- 散步、太极\n- 增强体质', '春季是调理身体的好时机，做好保健工作', '春季,养生,保健', 0, 'published', 35, '2025-02-26 11:00:00', '2025-02-25 19:00:00'),
('患者权利与义务', 'article', '# 患者权利与义务\n\n## 患者权利\n- 知情权\n- 选择权\n- 隐私权\n- 投诉权\n\n## 患者义务\n- 配合治疗\n- 遵医嘱用药\n- 诚实告知病情\n- 尊重医护人员', '了解患者权利和义务，建立良好医患关系', '权利,义务,医患', 0, 'published', 29, '2025-02-27 09:00:00', '2025-02-26 17:30:00'),

-- 添加平台公告
('平台服务升级通知', 'announcement', '# 平台服务升级通知\n\n亲爱的用户和陪诊员：\n\n为了提供更好的服务，我们将于下周进行系统升级。\n\n**升级内容：**\n- 优化订单匹配算法\n- 改进支付流程\n- 增强数据安全\n\n**升级时间：** 2025年3月1日 23:00-2025年3月2日 02:00\n\n感谢您的支持！', '系统升级，服务更优', 'targets:users,companions', 9, 'published', 156, '2025-02-28 10:00:00', '2025-02-27 20:00:00'),
('用户隐私保护政策更新', 'announcement', '# 用户隐私保护政策更新\n\n我们重视您的隐私安全。\n\n**更新内容：**\n- 加强数据加密\n- 完善隐私设置\n- 明确数据使用范围\n\n**生效时间：** 2025年3月1日\n\n请查看完整政策了解详情。', '隐私保护升级', 'targets:users,companions', 8, 'published', 98, '2025-02-28 11:00:00', '2025-02-27 21:00:00'),

-- 添加草稿状态的内容
('新功能预告：AI健康助手', 'article', '# 新功能预告：AI健康助手\n\n我们正在开发一个AI健康助手功能，将于下月上线。\n\n**功能特性：**\n- 24小时在线咨询\n- 智能症状分析\n- 个性化健康建议\n\n敬请期待！', '新功能即将上线', '新功能,AI,健康', 0, 'draft', 0, NULL, '2025-02-28 15:00:00'),
('陪诊员招聘启事', 'announcement', '# 陪诊员招聘启事\n\n我们正在招聘专业的陪诊员。\n\n**要求：**\n- 医学或护理相关背景\n- 有陪诊经验优先\n- 热心服务\n\n**福利：**\n- 灵活工作时间\n- 竞争力薪资\n- 完善培训\n\n欢迎投递简历！', '招聘陪诊员', 'targets:companions', 7, 'draft', 0, NULL, '2025-02-28 16:00:00');

-- ==================== 4. 评价表改进 ====================
-- 添加隐藏评价
INSERT INTO evaluation (order_id, user_id, companion_id, professionalism_score, attitude_score, efficiency_score, satisfaction_score, average_score, evaluation_text, status, create_time) VALUES
(1, 1, 1, 3, 2, 3, 2, 2.50, '陪诊员迟到了15分钟，服务态度一般', 'hidden', '2025-02-10 16:30:00'),
(2, 2, 2, 4, 3, 4, 3, 3.50, '陪诊员虽然专业，但沟通不够充分', 'hidden', '2025-02-11 18:00:00');

-- ==================== 5. 支付记录表改进 ====================
-- 添加支付失败记录
INSERT INTO payment (payment_no, order_id, order_no, user_id, amount, payment_channel, payment_status, transaction_id, paid_time, voucher_no, description, create_time) VALUES
('PAY20250301001', 21, 'ORD20250301001', 1, 110.00, 'mock', 'failed', 'TXN20250301001_FAILED', NULL, NULL, '订单ORD20250301001支付失败', '2025-02-28 17:00:00'),
('PAY20250302001', 22, 'ORD20250302001', 2, 220.00, 'mock', 'refunded', 'TXN20250302001_REFUND', '2025-02-28 19:00:00', 'REFUND20250302001', '订单ORD20250302001已退款', '2025-02-28 18:30:00');

-- ==================== 6. 用户表补充 ====================
-- 添加更多用户（可选）
INSERT INTO user (user_account, user_password, user_name, user_avatar, user_profile, user_role, user_phone, user_email, status, create_time) VALUES
('user021', '5f4dcc3b5aa765d61d8327deb882cf99', '李明', 'https://api.example.com/avatar/21.jpg', '患者，需要陪诊服务', 'user', '13800138021', 'user021@example.com', 1, '2025-02-04 10:30:00'),
('user022', '5f4dcc3b5aa765d61d8327deb882cf99', '王芳', 'https://api.example.com/avatar/22.jpg', '患者，经常就医', 'user', '13800138022', 'user022@example.com', 1, '2025-02-04 11:20:00');

-- ==================== 7. 陪诊员表补充 ====================
-- 添加更多陪诊员（可选）
INSERT INTO companion (companion_account, companion_password, companion_name, companion_phone, id_card, qualification_type, work_years, specialties, service_area, real_name_status, qualification_status, rating, service_count, status, create_time) VALUES
('companion021', '5f4dcc3b5aa765d61d8327deb882cf99', '秦陪诊', '13900139021', '110101201001211254', 'caregiver', 2, '全科陪诊、协助', '北京市朝阳区', 'approved', 'approved', 4.3, 52, 1, '2024-12-21 18:00:00'),
('companion022', '5f4dcc3b5aa765d61d8327deb882cf99', '尤护理', '13900139022', '110101201101221255', 'nurse', 8, '妇科、产科', '北京市东城区', 'approved', 'approved', 4.7, 148, 1, '2024-12-22 18:30:00');

-- ==================== 验证数据 ====================
SELECT '✅ 数据改进完成！' AS result;
SELECT CONCAT('订单表: ', COUNT(*)) AS order_count FROM appointment_order;
SELECT CONCAT('聊天记录表: ', COUNT(*)) AS chat_count FROM chat_message;
SELECT CONCAT('平台内容表: ', COUNT(*)) AS content_count FROM content;
SELECT CONCAT('评价表: ', COUNT(*)) AS evaluation_count FROM evaluation;
SELECT CONCAT('支付记录表: ', COUNT(*)) AS payment_count FROM payment;
SELECT CONCAT('用户表: ', COUNT(*)) AS user_count FROM user;
SELECT CONCAT('陪诊员表: ', COUNT(*)) AS companion_count FROM companion;
