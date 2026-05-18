# Digital Treehole

数字树洞全栈项目（校赛版）。后端：Java 21 + Spring Boot 3 + MySQL。前端：Vue 3 + Element Plus（CDN 版，无需 Node）。

支持：匿名进入、文本/涂鸦多轮聊天、内容审核、危机预警、情感星空图、管理后台。

## 你先看这 8 个文件

```text
docs/beginner-guide.md
docs/frontend-handoff.md
docs/learning-map.md
docs/team-collaboration.md
docs/figma-design-brief.md
docs/github-quick-start.md
docs/ai-rag-handoff.md
docs/team-copy-message.md
```

作用分别是：

```text
beginner-guide.md       给你自己看的小白上手指南
frontend-handoff.md     给前端同学看的接口交接文档
learning-map.md         你学后端时的阅读顺序
team-collaboration.md   给你们 3 个人一起用的协作说明
figma-design-brief.md   给前端 / 设计同学的 Figma 设计骨架
github-quick-start.md   第一次把项目发到 GitHub 的操作说明
ai-rag-handoff.md       给 AI 同学看的 RAG 对接说明
team-copy-message.md    可以直接发到三人小群的消息
```

## 快速启动（两步跑完整项目）

### 第一步：启动后端

```powershell
cd path\to\digital-treehole
.\scripts\run-dev.ps1                     # MySQL root 无密码
.\scripts\run-dev.ps1 -DbPassword "密码"  # 有密码
```

后端起来后：`http://localhost:8080/swagger-ui.html`

### 第二步：启动前端（新开一个 PowerShell 窗口）

```powershell
cd path\to\digital-treehole
.\scripts\run-frontend.ps1                          # 默认端口 3000，对接本机后端
.\scripts\run-frontend.ps1 -Port 3000 -ApiUrl "http://10.x.x.x:8080"  # 局域网模式
```

脚本会自动打开浏览器：
- 用户端：`http://localhost:3000/index.html`
- 管理后台：`http://localhost:3000/admin.html`（账号 admin / admin123456）

前端是纯 HTML + CDN 方案，**不需要 Node.js，双击脚本即可**。

---

## 一键运行（仅后端）

在 PowerShell 里进入项目：

```powershell
cd path\to\digital-treehole
```

如果 MySQL root 没有密码：

```powershell
.\scripts\run-dev.ps1
```

如果 MySQL root 有密码：

```powershell
.\scripts\run-dev.ps1 -DbPassword "你的MySQL密码"
```

启动成功后打开：

```text
http://localhost:8080/swagger-ui.html
```

Swagger 是接口测试页面。它能让你看到所有接口，也能点按钮发请求。

## 一键测试

```powershell
.\scripts\run-test.ps1
```

看到 `BUILD SUCCESS` 说明项目基本正常。

测试使用 H2 内存数据库，不依赖本机 MySQL。

## 查看局域网地址

前端同学不在你电脑上时，不能用你的 `localhost`。先运行：

```powershell
.\scripts\show-lan-ip.ps1
```

它会输出类似：

```text
http://<后端电脑局域网IP>:8080
```

后端启动后，把这个地址发给前端同学。

## 默认管理员账号

```text
username: admin
password: admin123456
```

## 主要接口

```text
POST /api/v1/visitors/session
POST /api/v1/visitors/session/refresh
POST /api/v1/chat/sessions
GET  /api/v1/chat/sessions
GET  /api/v1/chat/sessions/{sessionId}/messages
POST /api/v1/chat/sessions/{sessionId}/messages
POST /api/v1/doodles
GET  /api/v1/chat/stats/star-map
POST /api/v1/admin/auth/login
GET  /api/v1/admin/risk-events
```

完整接口说明看：

```text
docs/api-contract.md
```

如果你要开始和队友协作，再看：

```text
docs/team-collaboration.md
docs/figma-design-brief.md
docs/github-quick-start.md
docs/ai-rag-handoff.md
docs/team-copy-message.md
```

## 项目结构

```text
src/main/java/com/compe/treehole/controller   接口入口
src/main/java/com/compe/treehole/service      业务逻辑
src/main/java/com/compe/treehole/model        数据模型和枚举
src/main/java/com/compe/treehole/mapper       数据库操作
src/main/java/com/compe/treehole/dto          前后端传输对象
src/main/resources/db/migration               数据库建表和演示数据
frontend-cdn/                                前端（Vue3 CDN版，无需Node）
  index.html                                 用户端入口
  admin.html                                 管理后台入口
  css/app.css                                全局样式（深色星空主题）
  js/api.js                                  统一接口封装
  js/pages/                                  用户端页面
  js/admin/                                  管理端页面
docs/                                        文档
scripts/                                     运行脚本
  run-dev.ps1                                启动后端
  run-frontend.ps1                           启动前端（纯PS，无需Node）
  run-test.ps1                               跑测试
  show-lan-ip.ps1                            查看局域网IP
```

## 现在已经完成什么

```text
匿名用户 JWT
管理员登录
聊天会话
文本消息
AI mock/fallback 回复
最近 6 条历史上下文
内容审核规则
危机预警规则
心理援助资源
涂鸦 PNG 上传
孤儿涂鸦清理
情感星空图数据
Swagger 接口文档
主链路测试
```

## 后续还要做什么

```text
接 AI 同学真实 HTTP 服务（目前用 mock/fallback）
补更多演示数据（种子数据已有，可按需加）
补系统设计说明书
补 PPT 和答辩口径
继续补测试
```
