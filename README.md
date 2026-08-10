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

## 支持的 Google Gemini

- [对话 (支持function)](https://ai.google.dev/tutorials/rest_quickstart)
- [流式对话 (支持function)](https://ai.google.dev/tutorials/rest_quickstart)
- [Veo 视频生成](https://ai.google.dev/gemini-api/docs/video)

## 支持的 openAI

> 支持函数调用，请参考 OpenaiTest（函数部分实现大量参考了 https://github.com/TheoKanning/openai-java ）

- [对话 (支持function)](https://platform.openai.com/docs/api-reference/chat/create)
- [流式对话 (支持function)](https://platform.openai.com/docs/api-reference/chat/streaming)
- [Responses API (含stream)](https://developers.openai.com/api/reference/responses/overview)
- [文件](https://platform.openai.com/docs/api-reference/files)
- [Assistant (含stream)](https://platform.openai.com/docs/api-reference/assistants)
- [Image](https://platform.openai.com/docs/api-reference/images)
- [Batch](https://platform.openai.com/docs/api-reference/batch)
- [Realtime session](https://platform.openai.com/docs/api-reference/realtime-sessions)
- [Video (Sora)](https://platform.openai.com/docs/api-reference/videos)

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
