-- 预置个人账号：昵称 星旅人，密码 123456（BCrypt 加密）
INSERT INTO star_user (nickname, password, avatar_url, exam_date, daily_goal)
SELECT '星旅人',
       '$2a$10$1CZUI7OBJff9vguBYuaTDekn1/sY7SyU6V/Yhc6eQfTWRMWubZANa',
       '/avatars/planet-1.svg',
       '2026-12-14',
       50
WHERE NOT EXISTS (SELECT 1 FROM star_user WHERE nickname = '星旅人');
