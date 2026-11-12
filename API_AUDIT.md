# ai-java-sdk 接口审计(2026-11-12,基于 2.6.0)

## 方法

能拿机器可读规格的就不读文档页,用脚本把 SDK 每个类的线上字段名(按各客户端的命名策略推出)和规格逐字段比对:

| 厂商 | 依据 |
|---|---|
| OpenAI | 官方 `openai/openai-openapi` 仓库的 `openapi.json`(当天更新,OpenAPI 3.1.0,245 路径 / 2272 schema)|
| Gemini | v1beta discovery 文档 |
| Claude | 官方 `anthropic-sdk-python` 的 `types/*.py`(从 spec 生成,两天前更新;稳定版 + beta 合并比对)|

**规格不等于线上。** 官方 OpenAPI 里还列着 `/realtime/sessions`,线上已经 404。所以每个疑似问题都再用真机验证;端点层额外做了一遍**无副作用的线上路由扫描**(POST 发必然校验失败的 body,路径参数用不存在的 id,不会创建任何资源)。

---

## P0 —— 实际坏了

调用必然失败,或者字段永远读不到。

| # | 问题 | 依据 |
|---|---|---|
| 1 | **OpenAI Realtime 整个不可用。** `POST /v1/realtime/sessions` 线上返回 `Invalid URL`,端点已被移除。现在是 `POST /v1/realtime/client_secrets`,返回 GA 形状 `{expires_at, session:{audio, output_modalities, max_output_tokens, ...}, value}`;SDK 的 `RealtimeSession` 还是旧 beta 形状(顶层 `voice` / `modalities` / `input_audio_format`…) | 真机 |
| 2 | **`VectorStore.bytes` 永远是 null。** 线上字段叫 `usage_bytes` | 真机 |
| 3 | **`VideoJob` 的 `createdAt` / `completedAt` / `expiresAt` / `remixedFromVideoId` 永远是 null。** 字段上显式写了 camelCase 的 `@JsonProperty("createdAt")`,覆盖了 snake_case 策略,规格是 `created_at` 等 | 规格(代理不转发 videos,无法真机) |
| 4 | **`GET /v1/responses`(列表)用 API key 必然 401。** 线上答 `must be made with a session key`,只认浏览器会话 key;公开规格里也没有这个端点 | 真机 |
| 5 | **`POST /images/variations` 已退役。** 规格原话 "This endpoint is retired and no longer available",替代是带 prompt 的 image edits | 规格(代理不转发) |

---

## P1 —— 重要缺口

功能常用、影响计费或正确性,或者**能请求却读不到结果**。

### 能请求、读不到结果(前后不一致)

| # | 问题 |
|---|---|
| 6 | OpenAI chat:能发 `logprobs`,但 `choice.logprobs` 读不到 |
| 7 | Gemini:能发 `responseLogprobs` / `logprobs`,但 `Candidate.logprobsResult` / `avgLogprobs` 读不到 |

### 计费相关

| # | 问题 |
|---|---|
| 8 | **Claude `Usage.server_tool_use` 缺失(稳定版字段)。** web search 这类服务端工具**按次计费**,调用次数读不到。流式的 `MessageDeltaUsage` 也有这个字段 |
| 9 | **OpenAI Responses API 的 `input_tokens_details.cache_write_tokens` 缺失**(还有 `cache_write_tokens_details`)。2.3.0 给 chat completions 补了,Responses 这边漏了 |
| 10 | OpenAI `PromptTokensDetails` 缺 `text_tokens` / `image_tokens`;`CompletionTokensDetails` 缺 `text_tokens` |
| 11 | OpenAI `Batch` 缺 `usage`、`model` |

### 正确性

| # | 问题 |
|---|---|
| 12 | **Claude 内容块缺 `is_error`。** 工具调用失败时没法把失败告诉 Claude,影响 agent 循环 |
| 13 | **OpenAI chat 响应读不到 `refusal`。** `choice.message` 和流式 `delta` 共用的 `ChatResponseMessage` 只有 `content` / `reasoningContent` / `toolCalls` 三个字段;模型拒答时 `content` 为空、原因在 `refusal` 里,SDK 用户只会看到一条空消息 |
| 14 | **Gemini `FunctionCall.id` / `FunctionResponse.id` 缺失。** 并行函数调用时没法把调用和响应对上 |
| 15 | OpenAI `ChatFunction.strict` 缺失(函数调用的结构化输出) |
| 16 | OpenAI chat 响应读不到 `annotations`(联网搜索的引用);Claude 内容块缺 `citations` |
| 17 | Gemini `Part.fileData` 缺失 —— 没法引用 Files API 上传的文件或 YouTube 链接 |

---

## P2 —— 其余缺字段

### OpenAI

- chat 请求:`parallel_tool_calls`、`store`、`service_tier`、`metadata`、`safety_identifier`、`prediction`、`modalities`、`audio`、`web_search_options`、`moderation`
- chat 响应 / 流式:`service_tier`、`metadata`、`moderation`;流式 `obfuscation`;`StreamOptions.include_obfuscation`
- Responses 请求:`image`、`access_programs`;响应:`output_text`、`moderation`、`prompt_cache_options`、`prompt_cache_diagnostics`、`tool_usage`、`input_transformations`、`prompt`、`image`、`access_programs`
- Responses output item:28 种变体并集里缺 27 个字段(`encrypted_content`、`result`、`revised_prompt`、`container_id`、`approval_request_id` 等)——**不丢**,`ResponseOutputItem` 有 `@JsonAnySetter` 兜底,能从 `getExtra()` 取到,只是没有类型化 getter
- `CreateBatchRequest.output_expires_after`;`Model.shutdown_date`;`OpenaiFile.expires_at`
- vector store:请求缺 `description`、`chunking_strategy`;文件缺 `attributes`、`chunking_strategy`、`usage_bytes`;file batch 请求缺 `files`、`attributes`、`chunking_strategy`
- Decisions 请求:`prompt_cache_key`、`prompt_cache_options`

### Gemini

- 请求:`serviceTier`、`store`、`labels`、`continuationToken`
- 响应:`responseId`、`modelVersion`、`modelStatus`(真机确实返回了,SDK 丢掉)
- Candidate:`finishMessage`、`citationMetadata`、`urlContextMetadata`、`tokenCount`、`groundingAttributions`、`continuationToken`
- Tool:`urlContext`、`googleMaps`、`fileSearch`、`computerUse`、`mcpServers`
- ToolConfig:`retrievalConfig`
- FunctionDeclaration:`parametersJsonSchema`、`response`、`responseJsonSchema`、`behavior`
- FunctionResponse:`parts`、`scheduling`、`willContinue`
- Part:`videoMetadata`、`mediaResolution`、`audioTranscription`、`speechMetadata`、`partMetadata`、`mediaProcessing`
- ExecutableCode / CodeExecutionResult:`id`;SafetyRating:`blocked`;GroundingMetadata:`imageSearchQueries`、`retrievalMetadata`、`googleMapsWidgetContextToken`;UsageMetadata:`serviceTier`
- Schema:`default`、`example`

### Claude

- 请求(beta):`compaction`、`thread`、`user_profile_id`、`workspace_id`
- 响应:`container`(复用 code execution 容器要用)、`context_management`、`diagnostics`(cache diagnostics 的结果)
- 流式 delta:`stop_details`、`container`
- 内容块:document 的 `title` / `context`、image 的 `transformations`、`caller`、`toolset_name`
- 工具定义:`eager_input_streaming`、`input_examples`
- Usage(beta):`iterations`、`fallback_credit`

---

## 已核实没问题(列出来以免误改)

- **Gemini `Tool` 的 `code_execution` / `google_search_retrieval` 是 snake_case**,而 Gemini 是 camelCase API。真机证实请求侧两种写法都收(proto-JSON 规则),**不是 bug**
- Gemini `Schema.enum`:比对工具报了缺失,是误报。注解写在 accessor 上而不是字段上,离线测试已证明线名是 `enum`
- 有意的厂商扩展字段:Grok 的 `search_parameters`,DeepSeek 的 `prompt_cache_hit/miss_tokens` 和 `reasoning_content`,百炼的 `cache_creation_input_tokens`
- Claude 的 `betas` 走 `anthropic-beta` 头,是正确传法(Python SDK 只是把它当参数收再转成头)
- Claude `ChatMessageContent` 多出的 `partial_json` / `stop_reason` / `stop_sequence`:这个类兼作流式 delta,有意共用
- OpenAI `ThreadRequest.messages`、`EditImageRequest.response_format`、`Response.store`:共用类或只读回显,无害
- Claude 的 `OutputConfig` 与稳定版 + beta 完全一致;Gemini 的 `Content`、`SafetySetting`、`ThinkingConfig`、`FunctionCallingConfig`、`ImageConfig`、`SpeechConfig` 完全一致

---

## 没法验证的部分

- **30 个 OpenAI 端点**这次没法真机验证,因为 `chat.vacuity.me` 代理不转发:Assistants、Threads、Runs 全部,Videos 全部,`images/variations`。规格把 Assistants 和 Videos 标了 deprecated,variations 标了 retired,Threads / Runs 没标
- DeepSeek、阿里云百炼:本机没凭据
- 类型层面(比如 int / float / string)只核了这次改动涉及的字段,其余只比对了字段名

---

## 处理结果(2.7.0)

用户决定:P0 + P1 + P2 全部处理;废弃接口标 `@Deprecated` 而不删除;Realtime 迁移到 GA。

### 规模

按 git 与上个版本逐类比对,这一版**新增 233 个字段,分布在 75 个类里**(OpenAI 136 / Gemini 74 / Claude 23)。改完后用同一个比对工具重跑:三家非废弃接口的缺失**全部清零**,剩下的差异都在上面"已核实没问题"一节里有据可查。

### 修复过程中新发现的问题(审计时没看到)

| 问题 | 怎么发现的 |
|---|---|
| 6 个派生方法(`getSingleContent()`、`isRefusal()`、`isCompleted()`、`getOutputItemOrItem()`、`getBillableOutputTokenCount()`、`Response.getOutputText()`)被 Jackson 当成字段写进 JSON;`getSingleContent()` 在 `choices` 为 null 时空指针 | 233 个字段的往返绑定测试 |
| `Response.getOutputText()` 只从 `output` 推导,忽略服务端直接给的 `output_text` | 同上 |
| 三个 `ChatFunction` 的手写 builder 设置不了新字段(`build()` 只拷贝固定列表) | 写真机测试时用 builder 撞上的 —— 绑定测试走的是 setter,抓不到 |
| OpenAI `strict` 要求参数类每个字段都加 `@JsonProperty(required = true)`,否则服务端拒绝 schema | 真机测试 |
| OpenAI `metadata` 只有 `store = true` 时才允许 | 真机探测 |

前三类都加了契约测试,以后再出现会直接失败:序列化契约(用 Jackson 自己的内省,要求每个序列化属性都有对应字段)、手写 builder 契约(逐字段经 builder 设置后检查 `build()` 结果)。

### 刻意没做的

- **已废弃接口的字段缺口**(Assistants / Threads / Runs / Videos / 旧 Realtime):这些接口这次标了 `@Deprecated`,而且代理不转发、没法真机验证。往要淘汰、又没法验证的接口上补字段,和"测试保证无误"冲突,所以只标废弃,不补字段
- Gemini 的 `enableAffectiveDialog` / `translationConfig` / `audioTranscriptionConfig` / `responseFormat`:2.6.0 已经决定不加(前一个直接 400,后三个被接受但看不出效果),这次沿用

### 验证

- **离线**:233 个新增字段逐个做 JSON → 对象 → JSON 往返绑定测试;序列化契约与手写 builder 契约;每个契约测试都用变异证实能抓到对应错误
- **真机**:15 个走 SDK 的测试(OpenAI 7 / Gemini 4 / Claude 4),覆盖全部能便宜触发的 P0 / P1
