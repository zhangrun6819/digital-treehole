# 小白上手指南

这份文档不是给老师看的，是给你自己看的。目标只有一个：你打开项目时，不再觉得它是黑盒。

## 1. 这个项目现在是什么

它现在是一个后端服务。

后端服务不是网页，它更像一个“服务窗口”：

- 前端页面负责展示按钮、输入框、聊天气泡。
- 后端负责接收请求、处理业务、保存数据、返回结果。
- 数据库负责长期保存匿名用户、聊天记录、涂鸦、审核规则。

所以你现在主要通过 Swagger 或前端同学的页面来“看见”后端。

## 2. 项目里最重要的文件夹

```text
src/main/java/com/compe/treehole
```

后端 Java 代码都在这里。

```text
src/main/resources/db/migration
```

数据库建表脚本在这里。项目启动时，Flyway 会自动按顺序执行。

```text
docs
```

给你、前端同学、答辩材料看的文档在这里。

```text
scripts
```

一键启动、一键测试、查看局域网 IP 的脚本在这里。

## 3. Java 代码怎么分

你先记这几个词就够了：

```text
Controller
```

前端调用接口时，最先进入 Controller。

例子：

```text
VisitorController
ChatController
DoodleController
```

```text
Service
```

真正处理业务逻辑的地方。

例子：

```text
ChatService
VisitorSessionService
ModerationService
RiskGuardService
```

```text
Entity
```

Java 里的数据库表对象。

例子：

```text
ChatMessage
ChatSession
VisitorProfile
```

```text
Mapper
```

和数据库沟通的接口。你可以先理解成“帮我们查表、插表、改表的工具”。

例子：

```text
ChatMessageMapper
ChatSessionMapper
```

```text
DTO
```

前端和后端之间传递的数据格式。

例子：

```text
SendMessageRequest
SendMessageResponse
```

## 4. 一条聊天消息是怎么走的

拿“用户发一段文字”举例：

```text
前端页面
  -> ChatController
  -> ChatService
  -> ModerationService 内容审核
  -> RiskGuardService 危机预警
  -> ChatMessageMapper 保存用户消息
  -> MockAiCompanionClient 生成 AI 回复
  -> ChatMessageMapper 保存 AI 回复
  -> 返回给前端展示
```

你现在最值得先看这 3 个文件：

```text
src/main/java/com/compe/treehole/controller/ChatController.java
src/main/java/com/compe/treehole/service/ChatService.java
src/main/java/com/compe/treehole/ai/MockAiCompanionClient.java
```

看懂这 3 个，你就能看懂项目主链路。

## 5. 怎么启动

先进入项目目录：

```powershell
cd path\to\digital-treehole
```

如果你的 MySQL root 没有密码：

```powershell
.\scripts\run-dev.ps1
```

如果你的 MySQL root 有密码：

```powershell
.\scripts\run-dev.ps1 -DbPassword "你的MySQL密码"
```

启动成功后打开：

```text
http://localhost:8080/swagger-ui.html
```

Swagger 是接口测试网页，不是最终产品页面。

## 6. 怎么测试

```powershell
.\scripts\run-test.ps1
```

看到 `BUILD SUCCESS` 就说明后端基本没坏。

## 7. 怎么给前端同学联调

先查你的局域网 IP：

```powershell
.\scripts\show-lan-ip.ps1
```

如果输出：

```text
http://<后端电脑局域网IP>:8080
```

那你就把这个地址发给前端同学。

前端同学还需要这份文档：

```text
docs/frontend-handoff.md
```

## 8. 你先学哪部分

你现在正在学 JavaSE，所以先别急着啃所有东西。

建议顺序：

```text
1. 看 DTO：理解前端传什么、后端回什么
2. 看 Controller：理解接口地址怎么写
3. 看 Service：理解业务流程怎么串起来
4. 看 Entity：理解 Java 对象怎么对应数据库表
5. 看 Mapper：理解怎么查数据库
```

第一阶段你重点掌握：

```text
Controller + DTO + Service
```

这已经是能真正参与项目的一块了。
