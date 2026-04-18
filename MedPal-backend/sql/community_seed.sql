USE medpal;

INSERT INTO community_post (user_id, title, content, image_urls, status, comment_count, like_count, create_time, update_time, is_delete)
VALUES
(1, '第一次带老人去华西挂号，有没有流程建议？', '家里老人下周要去华西医院复诊，我是第一次带老人去，担心现场排队太久。有没有朋友能分享一下当天就诊、检查、取药的顺序？如果有推荐靠谱的陪诊员也欢迎留言。', '/api/file/preview/order_attachment/1/community-post-1.jpg', 'published', 2, 5, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, 0),
(2, '妇幼就诊需要提前准备什么资料？', '准备带宝宝去成都市妇女儿童中心医院做复查，想问一下病历本、出生证明、医保卡这些是否都需要带齐？另外有人有推荐对儿童沟通比较温柔的陪诊员吗？', NULL, 'published', 1, 3, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY, 0),
(3, '大家会不会在就诊前先把问题写下来？', '我发现每次进诊室都紧张，容易忘记要问医生什么。最近准备把病情、症状持续时间、既往检查结果和想问的问题提前写在备忘录里，感觉效率高很多。你们还有别的经验吗？', NULL, 'published', 0, 2, NOW() - INTERVAL 8 HOUR, NOW() - INTERVAL 8 HOUR, 0)
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

INSERT INTO community_comment (post_id, user_id, parent_id, content, image_urls, status, like_count, create_time, update_time, is_delete)
VALUES
(1, 4, NULL, '建议至少提前半小时到院，先在门诊楼服务台确认挂号信息，再安排抽血和影像检查，会顺很多。', NULL, 'published', 2, NOW() - INTERVAL 40 HOUR, NOW() - INTERVAL 40 HOUR, 0),
(1, 5, NULL, '如果老人行动不便，最好提前准备轮椅。平台里我之前约过一位骨科方向的陪诊员，流程非常熟。', NULL, 'published', 1, NOW() - INTERVAL 30 HOUR, NOW() - INTERVAL 30 HOUR, 0),
(2, 6, NULL, '宝宝就诊我一般会把既往检查单、过敏史和最近一周用药记录都打印出来，医生看起来更方便。', NULL, 'published', 0, NOW() - INTERVAL 12 HOUR, NOW() - INTERVAL 12 HOUR, 0)
ON DUPLICATE KEY UPDATE update_time = VALUES(update_time);

INSERT INTO community_like (user_id, target_type, target_id, create_time, is_delete)
VALUES
(7, 'post', 1, NOW() - INTERVAL 36 HOUR, 0),
(8, 'post', 1, NOW() - INTERVAL 35 HOUR, 0),
(9, 'post', 1, NOW() - INTERVAL 34 HOUR, 0),
(10, 'post', 1, NOW() - INTERVAL 33 HOUR, 0),
(11, 'post', 1, NOW() - INTERVAL 32 HOUR, 0),
(7, 'post', 2, NOW() - INTERVAL 16 HOUR, 0),
(8, 'post', 2, NOW() - INTERVAL 15 HOUR, 0),
(9, 'post', 2, NOW() - INTERVAL 14 HOUR, 0),
(7, 'post', 3, NOW() - INTERVAL 6 HOUR, 0),
(8, 'post', 3, NOW() - INTERVAL 5 HOUR, 0),
(12, 'comment', 1, NOW() - INTERVAL 20 HOUR, 0),
(13, 'comment', 1, NOW() - INTERVAL 19 HOUR, 0),
(14, 'comment', 2, NOW() - INTERVAL 18 HOUR, 0),
(1, 'companion', 1, NOW() - INTERVAL 10 DAY, 0),
(2, 'companion', 1, NOW() - INTERVAL 9 DAY, 0),
(3, 'companion', 1, NOW() - INTERVAL 8 DAY, 0),
(4, 'companion', 2, NOW() - INTERVAL 7 DAY, 0),
(5, 'companion', 2, NOW() - INTERVAL 6 DAY, 0),
(6, 'companion', 3, NOW() - INTERVAL 5 DAY, 0),
(7, 'companion', 4, NOW() - INTERVAL 4 DAY, 0),
(8, 'companion', 4, NOW() - INTERVAL 3 DAY, 0),
(9, 'companion', 4, NOW() - INTERVAL 2 DAY, 0),
(10, 'companion', 5, NOW() - INTERVAL 1 DAY, 0)
ON DUPLICATE KEY UPDATE is_delete = VALUES(is_delete);

UPDATE community_post p
SET comment_count = (SELECT COUNT(*) FROM community_comment c WHERE c.post_id = p.id AND c.is_delete = 0),
    like_count = (SELECT COUNT(*) FROM community_like l WHERE l.target_type = 'post' AND l.target_id = p.id AND l.is_delete = 0);

UPDATE community_comment c
SET like_count = (SELECT COUNT(*) FROM community_like l WHERE l.target_type = 'comment' AND l.target_id = c.id AND l.is_delete = 0);
