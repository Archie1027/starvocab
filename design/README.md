# 星词 · 六级备考页面与架构图

## 打开四个交互页面

在 `frontend` 目录运行 `npm.cmd install`（如未安装依赖），再运行 `npm.cmd run dev -- --host 127.0.0.1`。浏览器打开：

| 场景 | 地址 | 页面截图 |
| --- | --- | --- |
| 登录 | `http://127.0.0.1:5173/demo/login` | [桌面](previews/login-desktop.png) · [手机](previews/login-mobile.png) |
| 今日任务 | `http://127.0.0.1:5173/demo/today` | [桌面](previews/today-desktop.png) · [手机](previews/today-mobile.png) |
| 单词学习 | `http://127.0.0.1:5173/demo/study` | [桌面](previews/study-desktop.png) · [手机](previews/study-mobile.png) |
| 测试与复盘 | `http://127.0.0.1:5173/demo/test` | [桌面](previews/test-desktop.png) · [手机](previews/test-mobile.png) |

页面中的卫星围绕星球缓慢运行；右上角可关闭动效，系统开启减少动态效果时默认关闭。登录只需输入非空密码即可进入演示，这是独立交互原型，不会调用登录 API。单词、题目与演示成绩是本地模拟数据。

## 打开三张后端图

双击 HTML 文件即可离线查看、缩放与导出：

1. [目标架构](diagrams/01-architecture.html)（[PNG](diagrams/01-architecture.visual-check.1440x900.dark.png)）
2. [接口调用过程](diagrams/02-api-journey.html)（[PNG](diagrams/02-api-journey.visual-check.1440x900.dark.png)）
3. [学习数据流](diagrams/03-learning-dataflow.html)（[PNG](diagrams/03-learning-dataflow.visual-check.1440x900.dark.png)）

三张图中，登录/JWT/MySQL 用户表属于现有后端基础；词库、学习记录、测验与统计属于文档规划，尚未接入演示页面。

页面与图表文字已整体小幅放大。若根据图源 JSON 重新生成后端 HTML 图，运行 `python design/diagrams/adjust_typography.py` 可重新应用当前字号。

## 当前本地数据库

2026-10-11 已连接 `localhost:3306/starvocab` 核对：MySQL 8.0.46，库中只有 `star_user` 表（1 条用户记录），尚无 `star_word`。四个演示页面中的词条定义在 `frontend/src/demo/data.ts`。后续真实业务落地时，词条拟存 `star_word`，个人复习进度拟存 `orbit_record`，见 [需求文档](../需求文档.md)与[接口文档](../接口文档.md)。
