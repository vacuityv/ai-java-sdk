[[中文]](https://github.com/vacuityv/ai-java-sdk/tree/develop) [[English]](https://github.com/vacuityv/ai-java-sdk/blob/develop/README-eng.md)

# AI-Java-Sdk

> 欢迎体验个人对接的各大厂商AI的交互网站：[AI-CHAT 交互网站](https://chat.vacuity.me/)

为使用各大AI厂商提供的API创建的java sdk，目前支持 Anthropic Claude、Google Gemini、OpenAI，以及 xAI Grok、DeepSeek
等兼容 OpenAI 接口的厂商。

编译目标为 Java 8，可在 JDK 8 至 23 上构建。

## 支持的 Claude api

- [对话 (支持function)](https://docs.anthropic.com/claude/reference/messages_post)
- [流式对话 (支持function)](https://docs.anthropic.com/claude/reference/messages-streaming)
- 工具选择（`tool_choice`：强制指定工具 / 强制任意工具 / 禁用工具 / 关闭并行调用）
- [Prompt caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching)（system、消息内容块、工具定义三处均可设置缓存断点）
- [Adaptive thinking 与 effort](https://platform.claude.com/docs/en/build-with-claude/adaptive-thinking)
- [结构化输出](https://platform.claude.com/docs/en/build-with-claude/structured-outputs)（`output_config.format`）
- 拒绝原因（`stop_details`）、任务预算（`task_budget`）、上下文编辑、MCP server、fast mode 等
- `anthropic-beta` 头可按请求配置
- thinking 块前缀校验（`block_binding`）与思考 token 明细（`output_tokens_details`）

## 支持的 Google Gemini

- [对话 (支持function)](https://ai.google.dev/tutorials/rest_quickstart)
- [流式对话 (支持function)](https://ai.google.dev/tutorials/rest_quickstart)
- [Veo 视频生成](https://ai.google.dev/gemini-api/docs/video)
- 内置工具与函数声明同时使用（`toolConfig.includeServerSideToolInvocations`）

## 支持的 openAI

> 支持函数调用，请参考 OpenaiTest（函数部分实现大量参考了 https://github.com/TheoKanning/openai-java ）

- [对话 (支持function)](https://platform.openai.com/docs/api-reference/chat/create)
- [流式对话 (支持function)](https://platform.openai.com/docs/api-reference/chat/streaming)
- [Responses API (含stream)](https://developers.openai.com/api/reference/responses/overview)
- [文件](https://platform.openai.com/docs/api-reference/files)
- [Assistant (含stream)](https://platform.openai.com/docs/api-reference/assistants)
- [Image](https://platform.openai.com/docs/api-reference/images)（生成 / 编辑 / 变体，**生成与编辑支持流式**）
- [Batch](https://platform.openai.com/docs/api-reference/batch)
- [Realtime session](https://platform.openai.com/docs/api-reference/realtime-sessions)
- [Video (Sora)](https://platform.openai.com/docs/api-reference/videos)
- [Decisions API](https://developers.openai.com/api/docs/guides/decisions)（结构化判断，仅 gpt-6-luna）

## Importing

### Maven

```xml
<dependency>
    <groupId>me.vacuity.ai.sdk</groupId>
    <artifactId>ai-java-sdk</artifactId>
    <version>${version}</version>       
</dependency>
```

在这里可以查看最新的版本号：[Maven Central](https://central.sonatype.com/artifact/me.vacuity.ai.sdk/ai-java-sdk)

## 使用

普通对话：

```java

@Test
public void chat() {
    ClaudeClient client = new ClaudeClient(API_KEY);
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage("user", "introduce yourself pls"));
    ChatRequest request = ChatRequest.builder()
            .model("claude-opus-5")
            .messages(messages)
            .maxTokens(1024)
            .build();
    try {
        ChatResponse response = client.chat(request);
        System.out.println(response);
    } catch (VacSdkException e) {
        // detail 是服务端返回的原始错误对象，Claude 下为 ChatResponseError
        if (e.getDetail() instanceof ChatResponseError) {
            ChatResponseError detail = (ChatResponseError) e.getDetail();
            System.out.println(detail.getError().getMessage());
        }
    }
}
```

流式对话：

```java

@Test
public void streamChat() {
    ClaudeClient client = new ClaudeClient(API_KEY);
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage("user", "鲁迅为什么打周树人"));
    ChatRequest request = ChatRequest.builder()
            .model("claude-opus-5")
            .messages(messages)
            .maxTokens(1024)
            .build();
    Flowable<StreamChatResponse> response = client.streamChat(request);
    response.doOnNext(s -> {
        if ("content_block_delta".equals(s.getType())) {
            ChatMessageContent content = s.getDelta();
            System.out.print(content.getText());
        } else if ("error".equals(s.getType())) {
            System.out.println(s.getError().getMessage());
        }
    }).blockingSubscribe();
}
```

### 强制模型调用指定工具

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .messages(messages)
        .maxTokens(1024)
        .tools(tools)
        .toolChoice(ToolChoice.tool("get_weather"))
        .build();
```

`ToolChoice` 另有 `auto()`（默认，由模型决定）、`any()`（必须调用某个工具）、`none()`（禁止调用工具）。
需要限制每轮只调用一个工具时，设置 `setDisableParallelToolUse(true)`。

### Prompt caching

缓存读取的价格约为基础输入价的 0.1 倍。把稳定不变的内容（系统提示词、工具定义）放在前面并设置缓存断点，
把每次都变化的内容放在断点之后。

```java
ChatMessageContent system = new ChatMessageContent();
system.setType("text");
system.setText(longSystemPrompt);
system.setCacheControl(CacheControl.ephemeral("1h"));   // 也可用 ephemeral() 走默认的 5 分钟

ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .system(Collections.singletonList(system))
        .messages(messages)
        .maxTokens(1024)
        .build();

ChatResponse response = client.chat(request);
// 验证是否命中缓存；若多次相同请求该值始终为 0，说明前缀里有内容在变
System.out.println(response.getUsage().getCacheReadInputTokens());
```

缓存断点也可以设置在 `ChatMessageContent`（消息内容块）、`ChatFunction`（工具定义）上，
或直接设在 `ChatRequest` 顶层（自动作用于最后一个可缓存的块）。

### 思考过程与 effort

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .messages(messages)
        .maxTokens(8192)
        .thinking(Thinking.adaptive())                              // 由模型自行决定思考深度
        .outputConfig(OutputConfig.builder().effort("high").build())  // low / medium / high / xhigh / max
        .build();
```

需要拿到思考摘要时用 `Thinking.adaptive("summarized")`；默认为 `"omitted"`，此时仍会返回 thinking
块但文本为空。注意摘要是否生成由服务端决定，并非每次都有。

`Thinking.budgetTokens` 已废弃：在 Opus 4.7 及以后、Opus 5、Sonnet 5 等模型上发送会返回 400，请改用
`effort`。同理，`temperature` / `topP` / `topK` 在这些模型上也已不再接受。

### 使用 beta 能力

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .messages(messages)
        .maxTokens(1024)
        .betas(Arrays.asList("context-management-2025-06-27"))
        .build();
```

多个 beta 标记会以逗号拼接进 `anthropic-beta` 头。不设置时保持默认行为。

### GPT Image 流式生成

生图通常要十几秒。流式可以在最终图出来之前先拿到中间图，实测首图能提前约 5 秒。

```java
CreateImageRequest request = CreateImageRequest.builder()
        .model("gpt-image-1")          // 仅 gpt-image 系列支持流式，dall-e 不支持
        .prompt("a simple orange fox, flat vector style")
        .size("1024x1024")
        .partialImages(2)              // 中间图张数上限，0-3
        .build();

client.streamCreateImage(request).blockingSubscribe(event -> {
    if (event.isCompleted()) {
        save(event.getB64Json());                     // 最终图
        log(event.getUsage().getTotalTokens());       // 用量只在完成事件上
    } else {
        preview(event.getB64Json());                  // 中间图
    }
});
```

编辑也支持流式：

```java
client.streamEditImage(request, imageFile, maskFile)
```

**每个事件携带的都是一张完整独立的图，不是增量片段。** 这点和文本流式相反：渲染时应当整张替换（换掉 `img` 的 src），而不是追加拼接。

三个注意点：

- `partialImages` 是上限而非保证，服务端可能只回 1 张甚至 0 张，代码不要假设张数
- `usage` 只出现在完成事件上，中间事件里是 null
- 流量不小，一次 1024x1024 的低质量生图两个事件合计约 3.6MB base64，移动端慎用高 `partialImages`

流式方法依赖内部的 OkHttpClient，因此**不能用 `new OpenaiClient(OpenaiApi)` 这个构造器**，否则会抛 `IllegalStateException`。

### Claude 多轮对话的 thinking 块校验

自 2026-08-31 起创建的账号，服务端默认强制校验 thinking 块的前缀：一旦你改动了 system、tools 或历史消息，该块失效并返回 400。官方说明后续模型会对所有账号强制。

如果你的多轮逻辑会重建历史，可以选择降级而非报错：

```java
Thinking thinking = Thinking.adaptive();
thinking.setBlockBinding(BlockBinding.dropBlock());   // 默认是 error()

ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .thinking(thinking)
        .betas(Arrays.asList("thinking-binding-controls-2026-08-01"))
        .messages(messages)
        .maxTokens(4096)
        .build();
```

被丢弃的块会列在响应的 `getInputTransformations()` 里。另外 `usage.getOutputTokensDetails().getThinkingTokens()` 可以看出计费的输出 token 中有多少花在了思考上。

### Decisions API（结构化判断）

`POST /v1/decisions`，不返回自由文本，而是从你定义的选项里挑一个并给出概率分布。适合分类、路由、Agent 下一步决策。目前**只有 `gpt-6-luna` 支持**，且**只收输入费**（$0.10/1M），输出与缓存读写不计费。

```java
Decision decision = client.createDecision(DecisionRequest.builder()
        .model("gpt-6-luna")
        .input("The app crashes every time I open settings on iOS 18.")
        .questions(Arrays.asList(
                DecisionQuestion.predicate("is_bug", "Does this describe a defect?"),
                DecisionQuestion.choice("area", "Which area?", Arrays.asList(
                        DecisionQuestion.Choice.of("mobile", "iOS or Android client"),
                        DecisionQuestion.Choice.of("backend", "server side"))),
                DecisionQuestion.score("severity", "How severe?", Arrays.asList(
                        DecisionQuestion.Level.of("low", "cosmetic"),
                        DecisionQuestion.Level.of("medium", "degraded but usable"),
                        DecisionQuestion.Level.of("high", "unusable or data loss")))))
        .build());

DecisionAnswer area = decision.answer("area");
if (!area.isRefusal()) {
    System.out.println(area.getChoice() + " @ " + area.getConfidence());
}
```

三种题型：

| 类型 | 额外字段 | 返回 |
|---|---|---|
| `predicate` | 无 | `probability`，陈述为真的概率 |
| `choice` | `choices[]` | `choice` + `confidence` + 各选项概率分布 |
| `score` | `levels[]`（低到高） | `score` + `confidence` + 各档概率 |

**`score` 返回的是按概率加权的平均值，不是档位索引。** 例如三档 low/medium/high 得到 1.94，表示"几乎确定是 high 但略有保留"。路由时可直接拿它跟阈值比较，比单个标签信息量大。因此 `levels` 的顺序是有含义的，必须从低到高传。

注意事项：

- 读取任何值字段前先判断 `isRefusal()` —— 拒绝时所有值字段都是 null
- 互相独立的问题可以放进同一个 `questions` 一次问完；有依赖关系的必须拆成多次请求
- 图片输入必须是内联 base64 data URL，**不支持 http(s) 链接和 `file_id`**
- `usage` 里有 `output_tokens` 和 `reasoning_tokens`，但官方说明这些不计费，仅供观测

### Gemini 内置工具与函数声明同时使用

`gemini-4-argon` 在同一请求里既挂函数声明又挂 `googleSearch` 时，必须开启
`toolConfig.includeServerSideToolInvocations`，否则直接 400：

> Please enable tool_config.include_server_side_tool_invocations to use Built-in tools with Function calling.

```java
ChatRequest request = ChatRequest.builder()
        .model("gemini-4-argon")
        .contents(contents)
        .tools(Collections.singletonList(
                Tool.functionsWithGoogleSearch(functions)))   // 函数声明 + 内置搜索
        .toolConfig(ToolConfig.includeServerSideToolInvocations())
        .build();
```

只要内置搜索的，用 `Tool.googleSearch()`。不设 `toolConfig` 时该字段不会出现在请求体里，老模型不受影响。

**开启后响应里会多出两种 part：**

```java
for (ChatMessageContentPart part : candidate.getContent().getParts()) {
    if (part.getToolCall() != null) {
        // 模型在服务端发起的工具调用，如 GOOGLE_SEARCH_WEB 及其 queries
    } else if (part.getToolResponse() != null) {
        // 该次调用的结果
    }
}
```

这两种 part **同样带 `thoughtSignature`**。Gemini 3 / 4 要求多轮对话把上一轮 model 的 part 原样回传，因此 `ChatMessageContentPart` 增加了未知字段兜底（`getExtra()`）：服务端返回但 SDK 尚未建模的字段会被保留并在回传时原样发出，不会被静默丢弃。

### Gemini 思考 token 与计费

Gemini 2.5 / 3.x / 4 的思考 token 在 `usageMetadata.thoughtsTokenCount` 里，**不包含在 `candidatesTokenCount` 中**，但 Google 按输出价计费。只按 `candidatesTokenCount` 计费会漏收思考部分。

```java
Usage usage = response.getUsageMetadata();

usage.getCandidatesTokenCount();        // 可见回答
usage.getThoughtsTokenCount();          // 思考部分，模型未思考时为 null
usage.getBillableOutputTokenCount();    // 两者之和，即按输出价计费的总量
```

还补齐了 `cachedContentTokenCount`、`toolUsePromptTokenCount`、`trafficType`，以及四组 `ModalityTokenCount` 明细（`promptTokensDetails` / `candidatesTokensDetails` / `cacheTokensDetails` / `toolUsePromptTokensDetails`）。字段缺失时为 null。

**流式计费要注意两点：**

1. `usageMetadata` 出现在**每一个 chunk** 上，不是只在最后一个
2. 其中的计数是**累计值，不是增量** —— 逐 chunk 累加会严重超收

所以取最后一个 chunk 的 `usageMetadata` 即可：

```java
final Usage[] last = new Usage[1];
client.streamChat(request).blockingSubscribe(chunk -> {
    if (chunk != null && chunk.getUsageMetadata() != null) {
        last[0] = chunk.getUsageMetadata();      // 覆盖，不累加
    }
});
```

**模型行为差异**：`gemini-4-argon` 默认不思考，只设 `includeThoughts` 不会产生思考 token，必须同时设 `thinkingLevel`（low / medium / high）；`gemini-3.8-flash` 默认即思考。

### Claude 流式的截断判断

`message_delta` 事件的 `delta` 现在能读到 `stop_reason` 和 `stop_sequence`，不必再靠 `output_tokens >= max_tokens` 间接推断：

```java
client.streamChat(request).blockingSubscribe(event -> {
    if ("message_delta".equals(event.getType())) {
        String reason = event.getDelta().getStopReason();   // max_tokens / end_turn / tool_use ...
        if ("max_tokens".equals(reason)) {
            // 被截断
        }
    }
});
```

内容增量事件（`text_delta` / `thinking_delta` / `input_json_delta` / `signature_delta`）的解析不受影响，这两个字段在它们上面为 null。

### Claude API 版本（2.0.0 起默认 2026-10-21）

2.0.0 起，`anthropic-version` 头由 `2023-06-01` 改为 **`2026-10-21`**，并**移除了 `ChatRequest` 上的 `temperature` / `topP` / `topK` 三个字段**。

该 API 版本不再接受这三个参数，带上就返回 400，**即使传的是原来的默认值**（实测 `temperature: 1.0` 同样 400，报 `Extra inputs are not permitted`）。既然无法再发送，字段就一并删掉了——升级后这些调用会在**编译期**报错，而不是等到运行时。

```java
// 2.0.0 起不再存在，编译不通过
ChatRequest.builder().model("claude-opus-5").temperature(0.2f)

// 改为直接去掉
ChatRequest.builder().model("claude-opus-5").maxTokens(1024).build();
```

官方说明：新模型（Claude 4.7 及以后）本来就只接受默认值，这三个参数对它们无意义；老模型上降低 `temperature` 的效果会失去，且**没有替代参数**。

`anthropic-version` 仍可按需指定，但注意它**不能**恢复采样能力（字段已不存在）：

```java
ClaudeClient client = new ClaudeClient(
        API_KEY, Duration.ofSeconds(60), null, ClaudeClient.LEGACY_ANTHROPIC_VERSION);
```

它的用途是别的场景，例如 Amazon Bedrock 上早于 Claude 4.7 的模型不被 `2026-10-21` 覆盖。

该版本其余变更对本 SDK 无影响：user 消息里的 `citations`（本 SDK 无此字段）、Admin API 字段重命名（未覆盖）。但如果你把 `document` 块放在 `assistant` 消息里，新版本会返回 400——文档块应放在 `user` 消息中。

认证方式不变，仍用 `x-api-key`。官方 SDK 改用 `Authorization: Bearer` 是它们内部的变化，API 两种头都接受。

### between_tools 思考模式

`Thinking.betweenTools()` 关闭「回答前的思考」，但保留模型在工具调用之间写的进度更新。

```java
ChatRequest.builder()
        .model("claude-sonnet-5-5")
        .thinking(Thinking.betweenTools())
        .tools(tools)
        .maxTokens(2048)
        .build();
```

**仅 Claude Sonnet 5.5 支持**，其他模型一律 400。之所以存在，是因为 Sonnet 5.5 不接受 `Thinking.disabled()`，`between_tools` 是它唯一的「降低思考」手段。限制：只在 effort 为 `high` 或更低时可用，且不接受 `display` 等其他字段。请求里没有工具时，响应就只有文本。

发送前可用 Models API 的 `capabilities.thinking.types.between_tools.supported` 确认模型是否支持。

openAI vision：

```java

@Test
public void vision() throws IOException {
    String imagePath = "222.jpg";
    Path path = Paths.get(imagePath);
    // read file
    byte[] imageBytes = Files.readAllBytes(path);

    InputStream is = new BufferedInputStream(new FileInputStream(imagePath));
    String mimeType = URLConnection.guessContentTypeFromStream(is);

    // convert image to base64 data
    String base64Image = Base64.getEncoder().encodeToString(imageBytes);
    base64Image = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
    String url = "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg";

    OpenaiClient client = new OpenaiClient(API_KEY, Duration.ofSeconds(120));
    List<ChatMessage> messages = new ArrayList<>();

    ChatMessageContent content = new ChatMessageContent();
    ChatMessageContent.ImageUrl imageUrl = new ChatMessageContent.ImageUrl();
    // imageUrl.setUrl(url);
    imageUrl.setUrl(base64Image);
    content.setType("image_url");
    content.setImageUrl(imageUrl);
    ChatMessageContent content2 = new ChatMessageContent();
    content2.setType("text");
    content2.setText("what is this?");

    ChatMessage chatMessage = new ChatMessage("user", Arrays.asList(content, content2));
    messages.add(chatMessage);

    ChatRequest request = ChatRequest.builder()
            .model("gpt-4o")
            .messages(messages)
            .build();
    Flowable<StreamChatResponse> response = client.streamChat(request);
    response.doOnNext(s -> {
        System.out.println(s.getSingleContent());
    }).blockingSubscribe();
}
```

### 自定义地址和超时时间

```java
ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(100), "https://example.com");
```

### 使用http代理

```java

@Test
public void proxyChat() {
    String host = "127.0.0.1";
    int port = 7890;
    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));

    ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(60), proxy);

    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage("user", "introduce yourself pls"));
    ChatRequest request = ChatRequest.builder()
            .model("claude-opus-5")
            .messages(messages)
            .maxTokens(1024)
            .build();
    try {
        ChatResponse response = client.chat(request);
        System.out.println(response.getContent().get(0).getText());
    } catch (VacSdkException e) {
        // detail 是服务端返回的原始错误对象，Claude 下为 ChatResponseError
        if (e.getDetail() instanceof ChatResponseError) {
            ChatResponseError detail = (ChatResponseError) e.getDetail();
            System.out.println(detail.getError().getMessage());
        }
    }
}
```

## 从 1.9.x 升级到 1.10.0

`ChatRequest.getSystem()` 的返回类型由 `String` 改为 `Object`，以支持可设置缓存断点的 system text block。

建造器保留了 `system(String)` 重载，**既有的 builder 写法不受影响**；只有直接把 `getSystem()` 接成 `String`
的代码需要改：

```java
// 之前
String sys = request.getSystem();
// 之后
String sys = (String) request.getSystem();
```

## 其他

你可以在 ClaudeTest、GeminiTest、OpenaiTest / OpenaiAssistantTest 等测试类中查看代码示例（需要填入真实 key）。

`src/test/java/me/vacuity/ai/sdk/test/unit/` 下是不需要密钥的离线测试，`mvn test` 默认只运行这些。

## FAQ

### 支持什么模型

Claude、Google Gemini、OpenAI 的主流模型，以及 xAI Grok、DeepSeek 等兼容 OpenAI 接口的厂商。
请使用各厂商当前有效的模型 ID —— 已下线的模型会返回 404。

### openai 还有很多其他功能，这个sdk不支持吗

openai 目前在github上都有对应的sdk支持（比如：https://github.com/TheoKanning/openai-java ），所以不是紧急的需求，也许会在未来支持

## License

Published under the MIT License

## 如果您有余力，欢迎贡献代码 or Money

<img width="200" height="200" src="https://github.com/vacuityv/self-pay/blob/main/vac-wechat.jpg"/>


<img width="200" height="200" src="https://github.com/vacuityv/self-pay/blob/main/vac-alipay.jpg"/>
