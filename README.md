# 星词 StarVocab · 六级备考背单词工具

> 在紫色星云中开启六级备考旅程：每记住一个词，就在宇宙里点亮一颗星。

星词是一个**个人向**的六级备考背单词全栈应用。以「星球 / 轨道 / 星座」为隐喻组织整个学习模型：

- 单词是 **星辰（Star）**
- 一条 SM-2 记忆曲线是 **轨道（Orbit）**
- 一次词汇测验是 **星图测绘（Test）**
- 学习进度可视化是 **星云（Nebula）**
- 自定义词单是 **星集（Constellation）**

## 功能一览

| 模块 | 能力 |
| --- | --- |
| 词库 /stars | 六级核心词库（词频排序、字母/词性筛选、模糊搜索、词根词缀与近反义词详情） |
| 学习 /orbits | 今日队列（到期复习优先 + 高频新词）、逐层揭示学习卡、SM-2 四档自评、学习轨迹、重置 |
| 测验 /tests | 随堂小测 / 模拟考，英译中 · 中译英 · 拼写三种题型轮转，**答案仅后端保管**，交卷后逐题复盘 |
| 统计 /nebula | 学习概览、连续打卡、全年热力日历、进度星图（按首字母成座）、近 N 日准确率 |
| 词集 /constellations | 自建词单增删改、批量纳入星辰、官方星集 |
| 用户 /users | 注册登录（JWT）、个人资料、考试日期与每日目标、学习偏好（主题 / 动效） |

## 技术栈

**后端** `backend/` —— Java 21 · Spring Boot 3.3.5 · Spring Security + JJWT · MyBatis-Plus 3.5.7 · MySQL 8 · springdoc-openapi

**前端** `frontend/` —— React 18 · TypeScript · Vite 5 · react-router-dom 6 · Fetch 封装（自动携带 Bearer Token）· Web Speech API 发音

## 目录结构

```
StarVocab
├── backend/                # Spring Boot 后端
│   ├── src/main/java/com/starvocab/
│   │   ├── common/         # 统一响应 Result / 分页 PageResult / 全局异常
│   │   ├── config/         # Security · JWT 过滤器 · 分页插件 · 词库初始化器
│   │   ├── controller/     # REST 控制器（stars / orbits / tests / nebula / constellations / users）
│   │   ├── dto/            # 请求 / 响应 DTO
│   │   ├── entity/         # 数据库实体（7 张业务表）
│   │   ├── mapper/         # MyBatis-Plus Mapper
│   │   ├── service/        # 业务逻辑（含 Sm2Service 记忆算法）
│   │   └── util/           # JWT 工具 · JSON 解析
│   ├── src/main/resources/
│   │   ├── schema.sql      # 建表脚本（启动时自动执行）
│   │   ├── vocab/cet6-words.json   # 六级核心词库（空表时自动导入）
│   │   └── application.yml
│   └── src/test/           # JUnit 5 + Mockito 单元测试（45 例）
├── frontend/               # React 前端
│   ├── src/api/            # 请求封装与业务 API
│   ├── src/app/            # 正式页面（今日任务 / 学习 / 测验 / 词库）
│   ├── src/demo/           # 交互视觉原型（纯前端演示，可离线体验）
│   └── src/components/     # 星球背景等组件
├── design/                 # 设计资产（架构图 / API 时序 / 页面预览截图）
├── 需求文档.md              # PRD v1.1.0
└── 接口文档.md              # API 规范 v1.1.0（33 个接口 + SM-2 参考实现）
```

## 快速开始

### 环境要求

- JDK 21+、Maven 3.8+
- Node.js 18+
- MySQL 8（本地 `localhost:3306`）

### 1. 准备数据库

创建数据库即可，建表与词库导入由应用启动时自动完成：

```sql
CREATE DATABASE starvocab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库连接在 [backend/src/main/resources/application.yml](backend/src/main/resources/application.yml) 中配置，默认 `root / 123456`，按需修改。

### 2. 启动后端（8080）

```bash
cd backend
mvn spring-boot:run
```

首次启动会自动执行 `schema.sql` 建表，并导入 `vocab/cet6-words.json` 词库。

### 3. 启动前端（5173）

```bash
cd frontend
npm install
npm run dev
```

开发服务器已将 `/api` 代理到 `http://localhost:8080`，浏览器打开 <http://localhost:5173> 即可。

### 4. 登录

默认账号：**星旅人 / 123456**（也可在登录页注册新账号）。登录后从「今日任务」开始学习即可。

### 5. 运行单元测试

```bash
cd backend
mvn test
```

## SM-2 记忆算法

每张学习卡支持四档自评：**忘记(0) / 费力(1) / 迟疑(2) / 轻松(3)**。

- 评分 < 3：记忆重置，间隔回到 1 天
- 连续答对：1 天 → 6 天 → 间隔 × EF
- EF 增量 `0.1 - (3-r) × (0.08 + (3-r) × 0.02)`，下限 1.3
- 连续 5 次轻松回忆判定为「已掌握」，在进度星图中点亮

## 设计资产

- [design/README.md](design/README.md) —— 视觉体系说明
- [design/diagrams/](design/diagrams/) —— 系统架构图、API 调用时序、学习数据流（可交互 HTML）
- [design/previews/](design/previews/) —— 四大页面桌面端 / 移动端预览图

## 说明

- 本项目为个人学习用途，词库首期内置 124 个六级高频词，可在 `vocab/cet6-words.json` 中按现有格式扩充。
- 发音基于浏览器 Web Speech API，无需额外音频资源。
