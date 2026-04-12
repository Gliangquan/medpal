-- MedPal 数据库清空脚本
-- 清空所有表中的数据，保留表结构

USE medpal;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清空所有表
TRUNCATE TABLE chat_message;
TRUNCATE TABLE service_record;
TRUNCATE TABLE evaluation;
TRUNCATE TABLE appointment_order;
TRUNCATE TABLE doctor;
TRUNCATE TABLE department;
TRUNCATE TABLE hospital;
TRUNCATE TABLE user_settings;
TRUNCATE TABLE user;
TRUNCATE TABLE companion;
TRUNCATE TABLE payment;
TRUNCATE TABLE notification;
TRUNCATE TABLE content;
TRUNCATE TABLE price_config;
TRUNCATE TABLE system_config;

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ 所有表已清空！' AS result;

-- 验证
SELECT CONCAT('chat_message: ', COUNT(*)) AS count FROM chat_message;
SELECT CONCAT('service_record: ', COUNT(*)) AS count FROM service_record;
SELECT CONCAT('evaluation: ', COUNT(*)) AS count FROM evaluation;
SELECT CONCAT('appointment_order: ', COUNT(*)) AS count FROM appointment_order;
SELECT CONCAT('doctor: ', COUNT(*)) AS count FROM doctor;
SELECT CONCAT('department: ', COUNT(*)) AS count FROM department;
SELECT CONCAT('hospital: ', COUNT(*)) AS count FROM hospital;
SELECT CONCAT('user_settings: ', COUNT(*)) AS count FROM user_settings;
SELECT CONCAT('user: ', COUNT(*)) AS count FROM user;
SELECT CONCAT('companion: ', COUNT(*)) AS count FROM companion;
SELECT CONCAT('payment: ', COUNT(*)) AS count FROM payment;
SELECT CONCAT('notification: ', COUNT(*)) AS count FROM notification;
SELECT CONCAT('content: ', COUNT(*)) AS count FROM content;
