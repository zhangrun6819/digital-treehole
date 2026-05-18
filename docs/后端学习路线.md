# 学习路线图

这个项目后端可以按 5 小块学。

## 第 1 块：接口层

目标：知道前端请求怎么进后端。

先看：

```text
VisitorController
ChatController
```

你要记住：

```text
@RestController 表示这是接口类
@RequestMapping 表示接口前缀
@PostMapping 表示 POST 接口
@GetMapping 表示 GET 接口
@RequestBody 表示从请求体里拿 JSON
```

## 第 2 块：DTO

目标：知道前端传什么、后端回什么。

先看：

```text
CreateChatSessionRequest
SendMessageRequest
SendMessageResponse
```

你要记住：

```text
Request 通常是前端传给后端
Response 通常是后端返回给前端
```

## 第 3 块：业务层

目标：知道项目真正逻辑在哪里。

先看：

```text
ChatService
```

你要记住：

```text
Controller 不应该写很多业务逻辑
Service 才是处理业务流程的地方
```

## 第 4 块：数据库

目标：知道 Java 对象怎么保存到表。

先看：

```text
ChatMessage
ChatMessageMapper
V1__init_schema.sql
```

你要记住：

```text
Entity 对应数据库表
Mapper 负责操作数据库
Flyway 脚本负责建表
```

## 第 5 块：安全和演示稳定性

目标：知道 token、审核、兜底为什么重要。

先看：

```text
JwtService
AuthInterceptor
ModerationService
RiskGuardService
MockAiCompanionClient
```

你要记住：

```text
JWT 用来证明“我是谁”
审核用于处理不良内容
危机预警用于识别高风险表达
Mock AI 用来保证真实 AI 没接上时也能演示
```
