INSERT INTO content_rule (keyword, category, action, enabled, created_at, updated_at)
VALUES
('傻逼', 'INSULT', 'MARK', 1, NOW(), NOW()),
('滚开', 'INSULT', 'MARK', 1, NOW(), NOW()),
('色情', 'SEXUAL', 'BLOCK', 1, NOW(), NOW()),
('暴力威胁', 'VIOLENCE', 'BLOCK', 1, NOW(), NOW());

INSERT INTO risk_rule (keyword, risk_level, enabled, created_at, updated_at)
VALUES
('自杀', 'HIGH', 1, NOW(), NOW()),
('不想活', 'HIGH', 1, NOW(), NOW()),
('结束生命', 'HIGH', 1, NOW(), NOW()),
('伤害自己', 'MEDIUM', 1, NOW(), NOW());

INSERT INTO support_resource (title, contact, description, enabled, created_at, updated_at)
VALUES
('校内心理中心', '心理中心热线：123-4567-8901', '工作日 9:00-18:00，可预约心理咨询。', 1, NOW(), NOW()),
('24 小时心理援助热线', '400-161-9995', '遇到强烈危机感时，请优先联系可信任的人或专业热线。', 1, NOW(), NOW());

INSERT INTO admin_user (username, password_hash, display_name, enabled, created_at, updated_at)
VALUES
('admin', 'ac0e7d037817094e9e0b4441f9bae3209d67b02fa484917065f71b16109a1a78', '演示管理员', 1, NOW(), NOW());
