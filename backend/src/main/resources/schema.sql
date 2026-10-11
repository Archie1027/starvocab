-- 星词 StarVocab 数据库初始化脚本
-- 数据库：starvocab（需提前创建：CREATE DATABASE starvocab DEFAULT CHARACTER SET utf8mb4;）

-- 用户表
CREATE TABLE IF NOT EXISTS star_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    nickname        VARCHAR(64)  NOT NULL UNIQUE COMMENT '登录昵称',
    password        VARCHAR(128) NOT NULL        COMMENT 'BCrypt 加密密码',
    avatar_url      VARCHAR(256) DEFAULT '/avatars/planet-1.svg',
    exam_date       DATE         DEFAULT '2026-12-14',
    daily_goal      INT          DEFAULT 50,
    theme           VARCHAR(32)  DEFAULT 'planet-deep-space',
    motion_enabled  TINYINT      DEFAULT 1,
    accent          VARCHAR(8)   DEFAULT 'us',
    deleted         TINYINT      DEFAULT 0,
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 词库表（单词即星辰）
CREATE TABLE IF NOT EXISTS star_word (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    word          VARCHAR(64)  NOT NULL UNIQUE COMMENT '英文单词',
    phonetic_uk   VARCHAR(128) COMMENT '英音音标',
    phonetic_us   VARCHAR(128) COMMENT '美音音标',
    definition    TEXT         COMMENT '中文释义 JSON [{pos, meanings[]}]',
    example_en    TEXT         COMMENT '例句英文',
    example_cn    TEXT         COMMENT '例句中文',
    root          VARCHAR(128) COMMENT '词根',
    affix         VARCHAR(128) COMMENT '词缀',
    synonyms      VARCHAR(256) COMMENT '同义词（逗号分隔）',
    antonyms      VARCHAR(256) COMMENT '反义词',
    frequency     INT          DEFAULT 0 COMMENT '词频排名',
    cet6_count    INT          DEFAULT 0 COMMENT '六级真题出现次数',
    audio_url     VARCHAR(256) COMMENT '音频地址',
    difficulty    TINYINT      DEFAULT 3 COMMENT '难度 1-5',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='词库表';

-- 学习/复习记录（学习轨道）
CREATE TABLE IF NOT EXISTS orbit_record (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id        BIGINT       NOT NULL,
    word_id        BIGINT       NOT NULL,
    repetitions    INT          DEFAULT 0 COMMENT '连续答对次数',
    ease_factor    DECIMAL(4,2) DEFAULT 2.50 COMMENT '难度系数',
    interval_days  INT          DEFAULT 0 COMMENT '当前间隔天数',
    next_review    DATE         COMMENT '下次复习日期',
    last_review    DATETIME     COMMENT '上次复习时间',
    status         TINYINT      DEFAULT 1 COMMENT '0=新词 1=学习中 2=已掌握 3=待复习',
    total_reviews  INT          DEFAULT 0 COMMENT '累计复习次数',
    correct_count  INT          DEFAULT 0 COMMENT '答对次数',
    UNIQUE KEY uk_user_word (user_id, word_id),
    KEY idx_user_next (user_id, next_review)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习/复习记录';

-- 学习流水（每次评分一条，用于轨迹与统计）
CREATE TABLE IF NOT EXISTS orbit_log (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id       BIGINT   NOT NULL,
    word_id       BIGINT   NOT NULL,
    rating        TINYINT  COMMENT '0=忘记 1=费力 2=迟疑 3=轻松',
    mode          VARCHAR(16) DEFAULT 'flashcard' COMMENT '学习模式',
    ease_factor   DECIMAL(4,2) COMMENT '评分后难度系数',
    interval_days INT      COMMENT '评分后间隔天数',
    correct       TINYINT  DEFAULT 1 COMMENT '是否答对',
    reviewed_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_date (user_id, reviewed_at),
    KEY idx_user_word (user_id, word_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习流水';

-- 测试会话
CREATE TABLE IF NOT EXISTS test_session (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    type         VARCHAR(16) NOT NULL COMMENT 'quiz/exam/quick',
    total        INT         DEFAULT 0,
    correct      INT         DEFAULT 0,
    score        INT         DEFAULT 0,
    started_at   DATETIME    DEFAULT CURRENT_TIMESTAMP,
    finished_at  DATETIME,
    duration_sec INT         DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试会话';

-- 测试题目（答案仅存后端）
CREATE TABLE IF NOT EXISTS test_question (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id   BIGINT      NOT NULL,
    word_id      BIGINT      NOT NULL,
    type         VARCHAR(16) NOT NULL COMMENT 'en2cn/cn2en/spelling/choice',
    stem         VARCHAR(512) COMMENT '题干',
    options      VARCHAR(1024) COMMENT '选项 JSON 数组',
    answer       VARCHAR(256) COMMENT '正确答案',
    explanation  VARCHAR(512) COMMENT '解析',
    user_answer  VARCHAR(256) COMMENT '用户答案',
    correct      TINYINT     COMMENT '是否答对',
    cost_sec     INT         DEFAULT 0,
    sort_no      INT         DEFAULT 0,
    KEY idx_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试题目';

-- 词集（星集）
CREATE TABLE IF NOT EXISTS constellation_set (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       COMMENT '所属用户（NULL=官方共享）',
    name        VARCHAR(64)  NOT NULL,
    description VARCHAR(256),
    word_count  INT          DEFAULT 0,
    is_official TINYINT      DEFAULT 0,
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='词集';

-- 词集-单词关联
CREATE TABLE IF NOT EXISTS constellation_member (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    set_id   BIGINT NOT NULL,
    word_id  BIGINT NOT NULL,
    added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_set_word (set_id, word_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='词集-单词关联';
