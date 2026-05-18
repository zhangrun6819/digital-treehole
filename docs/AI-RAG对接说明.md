# AI / RAG 对接说明

最后更新：`2026-05-18`

这份文档专门给 AI 同学看。AI/RAG 服务不需要直接读取后端数据库，后端会在用户每次发消息时，把当前输入和最近上下文一起发给 AI 服务。

## 1. 现在怎么分工

| 模块 | 负责人 | 职责 |
| --- | --- | --- |
| Backend | 后端 | 保存会话、保存消息、发送上下文给 AI、保存 AI 回复、做内容审核和危机预警 |
| AI/RAG | AI 同学 | 接收后端请求、检索知识库、生成结构化回复 |
| Frontend | 前端 | 展示聊天、涂鸦、星空图、管理员页面 |

## 2. AI 服务不需要自己捞哪些数据

AI 服务不用直接查这些后端数据：

- `visitor_profile`
- `chat_session`
- `chat_message`
- `doodle_asset`
- `risk_event`

原因是：这些数据有权限边界，后端已经知道当前用户是谁、当前会话是哪一个。AI 服务直接查数据库会让权限和隐私变复杂。

正确做法是：后端每次调用 AI 时，把本次需要的上下文放进请求体。

## 3. 后端会给 AI 什么

后端内部现在的请求结构是：

```json
{
  "sessionId": 1,
  "companionStyle": "PEER",
  "historyMessages": [
    {
      "role": "USER",
      "content": "我最近因为考试很焦虑",
      "emotionTag": "ANXIOUS"
    },
    {
      "role": "ASSISTANT",
      "content": "我听到你了...",
      "emotionTag": "ANXIOUS"
    }
  ],
  "currentInput": {
    "inputType": "TEXT",
    "content": "我今天还是很紧张",
    "emotionTag": null,
    "shortNote": null,
    "doodleUrl": null
  }
}
```

字段说明：

| 字段 | 含义 |
| --- | --- |
| `sessionId` | 当前会话 ID，用于日志和追踪 |
| `companionStyle` | 陪伴风格：`GENTLE_ELDER`, `PEER`, `HUMOROUS` |
| `historyMessages` | 最近最多 `6` 条历史消息，按时间正序 |
| `historyMessages[].role` | `USER` 或 `ASSISTANT` |
| `historyMessages[].content` | 历史消息内容 |
| `historyMessages[].emotionTag` | 该消息情绪标签，可能为空 |
| `currentInput.inputType` | `TEXT` 或 `DOODLE` |
| `currentInput.content` | 文本输入内容；涂鸦时为空 |
| `currentInput.emotionTag` | 涂鸦时用户选择的情绪标签；文本时通常为空 |
| `currentInput.shortNote` | 涂鸦短说明 |
| `currentInput.doodleUrl` | 涂鸦图片地址；一期不强制视觉识别 |

## 4. AI 服务应该返回什么

返回结构必须固定，方便后端保存和前端展示：

```json
{
  "comfortText": "我听见你说今天还是很紧张，这种反复出现的焦虑确实会让人很累。",
  "reframeText": "转念一想，你能继续说出来，说明你已经在尝试把压力从心里拿出来一点。",
  "emotionTag": "ANXIOUS",
  "riskHint": "LOW",
  "followUpQuestion": "如果只选一件最让你紧张的事，会是哪一件？",
  "providerType": "HTTP"
}
```

字段要求：

| 字段 | 是否必填 | 规则 |
| --- | --- | --- |
| `comfortText` | 是 | 共情回复，建议 `80-180` 字 |
| `reframeText` | 是 | 转念建议，建议 `40-120` 字 |
| `emotionTag` | 是 | `CALM`, `SAD`, `ANXIOUS`, `ANGRY`, `HOPEFUL` |
| `riskHint` | 是 | `NONE`, `LOW`, `MEDIUM`, `HIGH` |
| `followUpQuestion` | 是 | 开放式追问，建议 `20-60` 字 |
| `providerType` | 否 | 可以固定返回 `HTTP` |

## 5. RAG 应该检索什么

RAG 的知识库建议先放这些内容：

- 校园心理咨询中心介绍
- 常见情绪问题科普，例如焦虑、失眠、考试压力、人际关系
- 非医疗性质的情绪调节建议
- 危机情况下的求助建议
- 项目内预设的陪伴话术模板

不要让 RAG 做这些事：

- 不要诊断疾病
- 不要开药或给医疗结论
- 不要承诺治疗效果
- 不要输出刺激性自伤细节

## 6. 推荐的 AI 服务接口

AI 同学如果要独立写 RAG 服务，建议先提供这个接口：

```http
POST /ai/v1/treehole/chat
Content-Type: application/json
```

请求体：使用第 3 节的结构。

响应体：使用第 4 节的结构。

第一轮联调先不用把 RAG 做得很复杂，只要服务能稳定接收请求并返回固定 JSON，就能先接上后端。后端后续只需要把 `AiCompanionClient` 切到这个 HTTP 服务。

## 7. 风险判断怎么处理

当前后端已经有本地风险规则，会根据关键词生成 `risk_event`。AI 返回的 `riskHint` 可以作为辅助信号，但第一版以“系统稳定可演示”为优先。

建议规则：

- AI 判断高风险时返回 `riskHint=HIGH`
- 后端本地规则命中高风险时也会生成风险事件
- 前端收到 `supportResources` 非空时展示心理援助信息

## 8. 联调前 AI 同学需要给后端什么

AI 同学给后端这 4 个信息就够：

```text
1. 服务地址，例如 http://127.0.0.1:9000/ai/v1/treehole/chat
2. 是否需要鉴权，例如 X-Api-Key
3. 超时时间建议，例如 8 秒
4. 一组真实 request/response 示例
```

拿到这些之后，后端再写一个真实 HTTP 客户端接入，不影响前端现在继续开发。
