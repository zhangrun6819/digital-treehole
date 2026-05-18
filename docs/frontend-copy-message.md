# 发给前端同学的消息

你可以直接把下面这段复制给前端同学。

```text
后端第一版接口已经出来了，你可以先按这份文档做页面和接口封装：

frontend-handoff.md

现在后端支持：
1. 匿名进入
2. 创建聊天会话
3. 发送文本消息
4. 上传涂鸦 PNG
5. 发送涂鸦消息
6. 获取情感星空图数据
7. 管理员登录和规则维护

如果你和后端在同一台电脑，接口地址是：
http://localhost:8080

如果你在自己的电脑跑前端，localhost 不是我的电脑，所以联调时要用我电脑的局域网地址，比如：
http://<后端电脑局域网IP>:8080

用户侧流程：
1. POST /api/v1/visitors/session 创建匿名身份，拿 token
2. 后续请求头带 Authorization: Bearer <token>
3. POST /api/v1/chat/sessions 创建会话
4. POST /api/v1/chat/sessions/{sessionId}/messages 发送文本或涂鸦消息
5. GET /api/v1/chat/stats/star-map?days=7 获取星空图数据

管理员账号：
admin / admin123456

现在是第一版接口，字段后续尽量不大改。你接的时候如果有不方便的地方，我们一起调整。
```

注意：不要只发 `D:\...` 这种本机路径。对方不在你的电脑上，看不到你的 D 盘。
