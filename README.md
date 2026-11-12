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
- [Decisions API](https://aistudio.google.com/docs/decisions-api)（结构化判断，仅 `gemini-decision-1`）
- 结构化输出（`responseSchema` / `responseJsonSchema`）、`imageConfig`、`speechConfig`、上下文缓存（`cachedContent`）

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

### Claude 逐条消息 effort（2.4.0，beta）

在对话中途改 effort，而**不打断缓存前缀** —— 这是它相对顶层 `output_config.effort` 的全部意义:逐条改动前面的内容原样不动,缓存还能命中;顶层改动会让缓存从头重建。

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5-5")
        .maxTokens(1024)
        .outputConfig(OutputConfig.builder().effort("high").build())   // 顶层保持不动
        .betas(Arrays.asList("mid-conversation-output-config-2026-07-01"))
        .messages(Arrays.asList(
                new ChatMessage("user", "..."),
                new ChatMessage("assistant", "..."),
                ChatMessage.effortOnly("low"),      // 从这里起降到 low
                new ChatMessage("user", "...")))
        .build();
```

`ChatMessage.effortOnly(effort)` 产出的就是文档里那个形状:`{"role":"system","content":[],"output_config":{"effort":"low"}}`。`content` **必须是空数组**,`output_config` 和 `role` / `content` 同级。也可以直接设字段:`.outputConfig(SystemMessageOutputConfig.effort("xhigh"))`。

**什么时候生效**:紧跟在一个带新输入的 `user` 轮之后,那一轮的回复就用新档位;放在别处(比如 `assistant` 轮和下一个 `user` 轮之间),从下一个带新输入的 `user` 轮开始生效。**只含 `tool_result` 的 `user` 轮不算新输入。** 之后一直有效,直到又有消息改它。

**放置位置**:effort-only 消息不渲染任何内容,所以豁免放置规则,放在 `messages` 第一条也可以(已实测)。但带文本的 system 消息不豁免;如果 effort-only 挨着一条带内容的 system 消息,两条一起按规则判定。

**会报 400 的几种情况**(都已实测):

| 情况 | 服务端报错 |
|---|---|
| 没带 beta 标记 | `messages.N.output_config: Extra inputs are not permitted` |
| 模型不支持 | `output_config.effort requires a model that supports per-turn effort; this model does not` |
| 和 `clear_at` 同时给 | `output_config is not permitted on a turn-scoped system message` |
| `effort` 传 `"adaptive"` | `Input should be 'low', 'medium', 'high', 'xhigh' or 'max'` —— `adaptive` 是 thinking 的类型,不是 effort 档位 |

**支持的模型**:`claude-fable-5-2`、`claude-mythos-5-2`、`claude-fable-5-1`、`claude-mythos-5-1`、`claude-opus-5-5`、`claude-opus-5`、`claude-sonnet-5-5`、`claude-haiku-5-5`。SDK 不维护自己的模型白名单,不支持的模型由服务端报错 —— 上游加新模型时不用等 SDK 发版。

另外两种拒绝来自 thinking 模式而不是模型:Claude Sonnet 5.5 配 `thinking.type` 为 `between_tools`、Claude Haiku 5.5 配 `disabled` 时,中途改 effort 会 400。

**注意**:在保留 thinking 的模型上,事后挪动或删掉这条消息,会让它之后所有 `assistant` 轮的 thinking 块失效 —— 只能往后追加,不要回头改历史。


### GPT Image 流式生成

生图通常要十几秒。流式可以在最终图出来之前先拿到中间图，实测首图能提前约 5 秒。

```java
CreateImageRequest request = CreateImageRequest.builder()
        .model("gpt-image-2.6-sunburst") // 仅 gpt-image 系列支持流式，dall-e 不支持
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

> `gpt-image-1` 已被上游下线（现在返回 `The model 'gpt-image-1' does not exist.`），示例已改用 `gpt-image-2.6-sunburst`。

### GPT-Image-2.6：GIF / SVG / 标注编辑

`gpt-image-2.6-sunburst` 与 `gpt-image-2.6-flare` 新增了两种输出格式和两个控制项。

**动图（GIF）**

```java
ImageResponse gif = client.createImage(CreateImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("a small waving cartoon cat")
        .outputFormat("gif")
        .frameCount(4)                 // 2-16，默认 9
        .frameDurationMs(150)          // 最小 20ms，无上限，默认 200
        .complexity("low")             // low / medium / high，默认 high
        .size("512x512")
        .build());

gif.getOutputFormat();                 // "gif"，服务端回显实际用的设置
gif.getData().get(0).getGenerationId();
```

帧是先画成一张雪碧图再合成的，所以 `frameCount` 和 `size` 不独立：雪碧图最长边不能超过 3840px。4 帧 + `1024x1024` 会被拒（`No supported sheet fits 4 GIF frames`），同样 4 帧在 `512x512` 下正常。撞到这个报错要降 `size`，而不是降帧数。

**矢量图（SVG）**

```java
ImageResponse svg = client.createImage(CreateImageRequest.builder()
        .model("gpt-image-2.6-flare")
        .prompt("a flat icon of a bicycle")
        .outputFormat("svg")
        .svg(SvgOptions.model("gpt-6.1-terra"))   // 必填，否则 400
        .build());

// b64_json 里是 base64 后的 SVG 源码文本，不是位图
String source = new String(Base64.getDecoder()
        .decode(svg.getData().get(0).getB64Json()), StandardCharsets.UTF_8);

svg.getSvgUsage().getModel();          // "gpt-6.1-terra"
svg.getSvgUsage().getTotalTokens();    // 按作图模型自己的价格单独计费
svg.getUsage().getTotalTokens();       // 与上面是两笔账，不要相加当成一笔
```

漏掉 `svg.model` 会直接返回 `missing_required_parameter`。SVG 只支持 generations，且 `n` 必须为 1。

**标注编辑（markup）**

`markup` 是一张和输入图同尺寸、带透明像素的 PNG，用来圈出要改的区域，比 `mask` 更灵活。它放在请求对象上而不是方法参数上，所以 `editImage` 的重载签名没有变化。

```java
client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("paint the circled area bright yellow")
        .markup(annotationPng)         // 与首张输入图同尺寸、含透明像素
        .complexity("low")
        .build(), sourceImage, (File) null);
```

`markup` 与 `mask` 互斥，服务端会返回 `mutually_exclusive_parameters`；SDK 在发请求前就会抛 `IllegalArgumentException`，省掉一次往返。

**其他**

- `safetyIdentifier` 是 `user` 的新名字，两者都还能用，也可以同时发；响应会回显
- 响应新增 `background` / `outputFormat` / `quality` / `size` 回显，请求没设时可以用它们看服务端取了什么默认值
- `usage.outputTokensDetails` 补齐了输出侧的 image/text token 拆分

> 2.1.0 的文档把 `frameCount` 写成 2-64、`frameDurationMs` 写成 20-2000ms，都是错的。按官方文档和实测：`frameCount` 是 **2-16**（生成和编辑都一样），`frameDurationMs` 最小 20 且**没有上限**（编码器按 10ms 取整，每帧编码延迟上限 655350ms，实测 5000 原样接受）。另外 `complexity` 没有固定默认值，省略时用的是该图像模型自己配置的默认。

### GIF 编辑（2.2.0）

编辑也能出动图，和生成同一组参数：

```java
ImageResponse gif = client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("make the shape pulse")
        .outputFormat("gif")
        .frameCount(4)
        .frameDurationMs(150)
        .quality("low")
        .size("512x512")
        .build(), sourceImage, (File) null);
```

这里的 `512x512` 和普通编辑不能互换：同尺寸的 PNG 编辑会被拒为 `below the current minimum pixel budget`，因为 GIF 的像素预算是按雪碧图算的，不是按单帧。GIF 编辑不支持 `mask` / `stream` / `partialImages` / `outputCompression`。

**SVG 编辑在 2.2.1 开始可用。** 它到 2026 年 11 月初还是直接被拒的（`Invalid value: 'svg'. Supported values are: 'png', 'webp', and 'jpeg'.`），现在 gpt-image-2.6 上已经支持 —— 这个变化是 SDK 自己的活体测试发现的。和 SVG 生成一样要 `svg.model` 和 `n=1`、不支持流式，但和生成不同,它接受 `mask`：

```java
ImageResponse svg = client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-flare")
        .prompt("redraw this as a flat vector icon")
        .outputFormat("svg")
        .svg(SvgOptions.of("gpt-6.1-terra", "low"))
        .build(), sourceImage, (File) null);
```

### SVG 作图模型的 reasoning（2.2.0）

`svg.reasoning.effort` 控制作图模型想多久，和 `complexity`（控制参考图）是两回事：

```java
ImageResponse svg = client.createImage(CreateImageRequest.builder()
        .model("gpt-image-2.6-flare")
        .prompt("a flat icon of a bicycle")
        .outputFormat("svg")
        .svg(SvgOptions.of("gpt-6.1-terra", "low"))   // 等价于 model + reasoning.effort
        .build());
```

`effort` 取 `none` / `minimal` / `low` / `medium` / `high` / `xhigh` / `max`，省略时用作图模型自己的默认。它直接影响账单：同一个 prompt，默认时 `svgUsage.reasoningTokens` 实测 501~1226，`low` 降到 57。

### 引用已上传的图做编辑（2.2.0）

编辑接口还有一条 JSON 形式的路径，输入图用 `file_id` 或 URL **引用**而不是上传字节。同一张图要反复编辑时，上传一次比每次重传划算 —— 这是 multipart 那条路做不到的。

```java
// 先上传一次
OpenaiFile uploaded = client.uploadFile("vision", "/path/to/mug.png");

// 之后按 id 引用，不传字节
ImageResponse result = client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("make the mug red")
        .images(Collections.singletonList(ImageReference.fileId(uploaded.getId())))
        .quality("low")
        .size("1024x1024")
        .build());
```

`ImageReference` 二选一：`fileId(..)` 或 `imageUrl(..)`（支持 `data:image/png;base64,...`，编码后 20MB 以内）。`images` 最多 16 张。这条路径上的 mask 和标注层用 `maskReference` / `markupReference`，不是文件字段：

```java
client.editImage(EditImageRequest.builder()
        .prompt("paint the circled area yellow")
        .images(Collections.singletonList(ImageReference.imageUrl(sourceDataUrl)))
        .markupReference(ImageReference.imageUrl(markupDataUrl))
        .build());
```

流式也支持，注意编辑发的是 `image_edit.*` 事件而不是 `image_generation.*`（`isCompleted()` 两者都认）：

```java
client.streamEditImage(request).blockingSubscribe(event -> { ... });
```

**两条路不能混着用。** `editImage(request)` 走 JSON，不能带 `markup` 文件字段；带 `java.io.File` 的那些重载走 multipart，不能带 `images` / `maskReference` / `markupReference`。设了对面那条路的字段会直接抛 `IllegalArgumentException`，而不是悄悄丢掉 —— `moderation` 从 1.9.17 到 1.11.0 就是这么没生效的。

### 关于客户端校验

SDK 只在两处抢在请求前报错：`svg` 输出缺 `svg.model`，以及 `mask` 和标注层同时给。其余格式限制（GIF 不能流式、SVG 要 `n=1`、透明背景不能配 jpeg 等）一律交给服务端。

2.2.0 的开发过程中这些规则曾经全部在客户端镜像过一遍，然后删掉了：镜像一条规则，就等于上游放宽之后 SDK 还在拦，而且会让活体测试失去发现这件事的能力。现在这些规则由 `OpenaiImageLiveSmokeTest` 钉住服务端当前的行为 —— 那里红了不代表 SDK 坏了，代表 API 变了。

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

### 兼容 OpenAI 的 chat-completions 缓存 token（2.2.1 起）

`Usage` 把各家官方文档里的字段**原样映射**,不做归一化 —— 命中数有两种形状,两种都收,不替你选一种,也不替你算:

```java
Usage u = client.chat(request).getUsage();

u.getPromptTokensDetails().getCachedTokens();              // OpenAI / 百炼 / DeepSeek
u.getPromptTokensDetails().getCacheWriteTokens();          // OpenAI,gpt-5.6 起
u.getPromptTokensDetails().getCacheCreationInputTokens();  // 百炼兼容模式的写入
u.getPromptCacheHitTokens();                               // DeepSeek 扁平形状
u.getPromptCacheMissTokens();
u.getCompletionTokensDetails().getReasoningTokens();
```

| 字段 | 谁用 | 核实依据 |
|---|---|---|
| `promptTokensDetails.cachedTokens` | OpenAI、百炼、DeepSeek | 三家官方文档 + OpenAI 真机 |
| `promptTokensDetails.cacheWriteTokens` | OpenAI,gpt-5.6 起 | gpt-5.6-terra 真机 |
| `promptTokensDetails.cacheCreationInputTokens` | 阿里云百炼兼容模式 | 百炼官方文档(**未真机验证**) |
| `promptCacheHitTokens` / `promptCacheMissTokens` | DeepSeek 扁平形状 | DeepSeek 官方文档 + deepseek-v4-flash 真机(由 Firefly 维护者跑的,非本仓库复现) |
| `completionTokensDetails` | `reasoningTokens` / `audioTokens` / `acceptedPredictionTokens` / `rejectedPredictionTokens` | OpenAI 真机 |

**包含关系按各家官方文档,互不相同,计费时请按你用的那家来算:**

- **OpenAI**:`promptTokens` **就是总数**,`cachedTokens` 与 `cacheWriteTokens` 是它互不重叠的子集。gpt-5.6-terra 实测同一 prompt 连发两次,`promptTokens` 都是 2655:

  | | 普通 | cached | cacheWrite |
  |---|---|---|---|
  | 第一次(写入) | 3 | 0 | 2652 |
  | 第二次(读取) | 3 | 2652 | 0 |

  三者价格不同,所以账单是三项:`普通 × 输入价 + cacheWrite × 输入价 × 写入倍率 + cached × 输入价 × 命中倍率`。gpt-5.6+ 写入 1.25×(12 小时 TTL 2.5×)、命中 0.1×(Sol / Terra 0.05×);更早的模型不额外收写入费。

- **DeepSeek**:官方文档写 `prompt_tokens = prompt_cache_hit_tokens + prompt_cache_miss_tokens`,并且 `cached_tokens` 文档原话是 "Same as `prompt_cache_hit_tokens`"。deepseek-v4-flash 真机印证:`1832 = 1664 命中 + 168 未命中`,两种形状都是 1664。DeepSeek 没有写入字段。

- **阿里云百炼**:官方文档写 `cached_tokens` 是 "`usage.prompt_tokens` 的一部分"。但**文档没写 `cache_creation_input_tokens` 和 `prompt_tokens` 的关系**,SDK 也不替你猜 —— OpenAI 的同类字段是子集,但那是 OpenAI 的行为,不是百炼的文档事实,按它计费前请用自己的流量核一次。百炼文档给的倍率:显式缓存写入 1.25×、命中 0.1×;隐式缓存写入 1.0×、命中 0.2×。

字段**没返回**时是 `null` 而不是 `0` —— "这个模型不报" 和 "这次是 0" 是两件事,gpt-5.5 就不报写入字段。

**流式要显式要求 usage。** 对 OpenAI,不设 `streamOptions.includeUsage` 时**任何 chunk 都不带 usage**,连最后一个也没有;设了之后会多出一个独立的 usage-only chunk,其余 chunk 的 `usage` 是 `null`:

```java
ChatRequest request = ChatRequest.builder()
        .model("gpt-5.5").messages(messages)
        .streamOptions(StreamOptions.builder().includeUsage(true).build())
        .build();

client.streamChat(request).blockingSubscribe(chunk -> {
    if (chunk.getUsage() != null) bill(chunk.getUsage());   // 只有最后那个非 null
});
```

DeepSeek 文档则说 usage 挂在最后一个内容 chunk 上,不另发 usage-only chunk。靠流式计费的代码两种都要能处理 —— 判断 `getUsage() != null` 而不是数 chunk。

### ⚠️ Claude 的缓存口径和 OpenAI 相反

跨厂商计费最容易算错的一处。SDK 不替你调和,所以这个差异要你自己处理:

| | 总输入 token 怎么算 |
|---|---|
| 兼容 OpenAI 侧 | `promptTokens` **就是总数**,缓存是子集 → **减** |
| Claude | `inputTokens` 是**已扣掉缓存的余数** → **加** |

```java
// 兼容 OpenAI 侧:promptTokens 是总数
openaiUsage.getPromptTokens();

// Claude:不要把 inputTokens 当总数
claudeUsage.getInputTokens()
        + claudeUsage.getCacheCreationInputTokens()
        + claudeUsage.getCacheReadInputTokens();
```

claude-opus-5-5 实测,约 4.8k token 的 system 带 `cache_control`,同一请求连发两次:

| | inputTokens | cacheCreation | cacheRead | 总输入 |
|---|---|---|---|---|
| 第一次(写入) | 13 | 4763 | 0 | 4776 |
| 第二次(读取) | 13 | 0 | 4763 | 4776 |

**两次 `inputTokens` 都是 13。** 按 OpenAI 那套把它当总数,这个请求就会按 13 个 token 计费而不是 4776。

### Claude 的缓存写入要看 TTL(2.3.0)

Claude 的写入价按生命周期分档:**5 分钟 1.25×、1 小时 2×**,所以 `cacheCreationInputTokens` 单独一个数没法定价。2.3.0 补了细分:

```java
claudeUsage.getCacheCreation().getEphemeral5mInputTokens();   // 1.25×
claudeUsage.getCacheCreation().getEphemeral1hInputTokens();   // 2×
```

两者相加等于 `cacheCreationInputTokens`。读取价约 0.1× 基础输入价(Opus 5.5 / Sonnet 5.5 是 0.05×,Fable / Mythos 5.1+ 是 0.025×),各模型不同,以官方价为准。

同时补了 `inferenceGeo` 和 **`speed`** —— 后者影响计费(fast 模式约 2× 价),而且报的是**实际服务的速度**,可能和请求的不一致(会回落到 standard),按它报的算。

### Gemini Decisions API（结构化判断）

`POST v1beta/decisions`，不返回自由文本，而是对你定义的问题给出校准后的概率。**只收输入费**——它完全不产生输出 token，`total_output_tokens` 恒为 0。目前只有 `gemini-decision-1` 支持。

```java
Map<String, String> causes = new LinkedHashMap<>();
causes.put("pool", "database connection pool exhaustion");
causes.put("deploy", "a bad deployment");
causes.put("network", "a network partition");

Map<String, DecisionQuestion> questions = new LinkedHashMap<>();
questions.put("is_outage", DecisionQuestion.noul("Is this an active production outage?"));
questions.put("root_cause", DecisionQuestion.choice("What is the most likely root cause?", causes));
questions.put("severity", DecisionQuestion.score("How severe is this?",
        Arrays.asList("negligible", "minor", "moderate", "major", "critical")));

Decision decision = client.createDecision(DecisionRequest.builder()
        .model("gemini-decision-1")
        .input("The server returned HTTP 500 on 40% of requests for the last 10 minutes. ...")
        .questions(questions)
        .build());

decision.answer("is_outage").getNoul();          // 0.95 —— “是”的概率，“否”是 1 减它
decision.answer("root_cause").getChoice();       // "pool"
decision.answer("root_cause").getProbabilities();// 整个分布，和为 1
decision.answer("severity").getScore();          // 3.11 —— 加权平均，是小数不是档位下标
decision.answer("severity").getLegend();         // {"0":"negligible", ... "4":"critical"}
decision.getUsage().getTotalOutputTokens();      // 恒为 0
```

**三种问题类型**:`noul`(是/否,Google 就叫这个名字)、`choice`(无序选项)、`score`(有序档位)。问题和答案都是**按 id 的 map**,不是列表 —— 这和 OpenAI 的 Decisions API 形状不同,SDK 按 Gemini 自己的样子映射,不往一起凑。

**`score` 的档位顺序是有意义的**:从低到高传,答案里的 `score` 是档位下标的概率加权平均,所以是小数。五档时 3.11 落在第四档稍过一点。

**多模态输入**:

```java
.input(Arrays.asList(
        DecisionInput.text("Screenshot from monitoring:"),
        DecisionInput.image(base64Png, "image/png", "low"),   // resolution 可选
        DecisionInput.document(base64Pdf)))                   // 仅 application/pdf
```

也支持 `audio` / `video`,以及用 URL 引用:`DecisionInput.uri("image", url, "image/png")` —— 注意 URL 是**由 Google 去抓**,得从他们那边能访问到,不是从你这边。

**两处官方文档不准确,已实测修正**:

1. **输入块必须带 `type`**。官方页描述 image 块时只列了 `data` / `uri` / `mime_type`,漏了必填的 `type`,漏掉会被拒:`The 'type' parameter is required at 'input[0]'`。SDK 的工厂方法都带上了。
2. **`noul` 的 `criteria` 不是没用的**。官方字段表说它对 `noul` 不使用,但示例里传了。实测示例是对的:同一问题不带 criteria 答 0.9514、带 0.9464,input token 从 119 涨到 149 —— 会发出去、会计费、会影响结果。所以提供了三参的 `DecisionQuestion.noul(instructions, trueDescription, falseDescription)`。

**这个端点是 snake_case**(`total_input_tokens` / `input_tokens_by_modality` / `mime_type`),和 Gemini 其他接口的 camelCase 不一样。SDK 的 Gemini mapper 没设命名策略,所以这些字段都显式标了 `@JsonProperty`,并有变异测试兜底 —— 漏一个就是静默读不到。

**不支持的东西**(官方列出):上下文缓存、Batch API、Flex / Priority 推理、对话历史、thought 块、工具调用块、YouTube 链接、`gs://` URI。问题的 `instructions` 只接受文本。

**限额**:问题 1–1000 条;上下文窗口 262144 token(输入与所有问题合计);choice 选项 2–255;score 档位 2–255(建议 2–10);inline 数据单请求 100MB、PDF 50MB;外部 URL 单个 15MB。安全拦截返回 HTTP 400 且**没有 `answers`**,所以在 SDK 里表现为抛异常,而不是返回一个带空洞的 `Decision`。

### Gemini GenerationConfig 补齐与 topK 类型修正(2.6.0)

按 v1beta **discovery 文档**(`generativelanguage.googleapis.com/$discovery/rest?version=v1beta`,机器可读的契约而非散文页)逐字段比对后补齐。

**`topK` 从 `Float` 改成 `Integer`。** discovery 里是 int32。改之前 `.topK(5.5f)` 能编译但必然 400:`Invalid value at 'generation_config.top_k' (TYPE_INT32), 5.5`。整数值的小数(`5.0f`)一直是被接受的,所以线上没发错东西 —— 只是类型允许了一个不可能成功的值。`topK` 还必须非负(`-1` 被拒)。

**结构化输出**:

```java
Map<String, Schema> props = new LinkedHashMap<>();
props.put("ok", Schema.builder().type("BOOLEAN").build());
props.put("why", Schema.builder().type("STRING").build());

.generationConfig(ChatRequest.GenerationConfig.builder()
        .responseMimeType("application/json")
        .responseSchema(Schema.builder().type("OBJECT").properties(props)
                .required(Collections.singletonList("ok"))
                .propertyOrdering(Arrays.asList("ok", "why"))   // 实测生效
                .build())
        .build())
```

`Schema` 的类型名是**大写**(`OBJECT` / `STRING` / `BOOLEAN`…),和普通 JSON Schema 的小写不同。`enum` 是 Java 关键字,字段叫 `enumValues`,线名仍是 `enum`。

想直接用普通 JSON Schema 就用 `responseJsonSchema`(discovery 里类型是 `any`,所以 SDK 收 `Object`,传 Map 即可)—— 它还支持 `additionalProperties: false`,而 `responseSchema` 没有这个字段:

```java
.responseJsonSchema(jsonSchemaAsMap)
```

**图片与语音**:

```java
.imageConfig(ImageConfig.builder().aspectRatio("16:9").imageSize("2K").build())
.speechConfig(SpeechConfig.voice("Kore"))                       // 单一音色
.speechConfig(SpeechConfig.builder().languageCode("en-US")      // 多说话人
        .multiSpeakerVoiceConfig(MultiSpeakerVoiceConfig.builder()
                .speakerVoiceConfigs(Arrays.asList(
                        SpeakerVoiceConfig.of("Alice", "Kore"),
                        SpeakerVoiceConfig.of("Bob", "Puck"))).build()).build())
```

TTS 返回的音频容器**按模型不同**:实测 `gemini-2.5-flash-preview-tts` 是 `audio/L16;codec=pcm;rate=24000`,`gemini-3.8-flash-tts` 是 `audio/wav` —— 读 part 的 mimeType,不要假定。

**上下文缓存**:`cachedContent` 是**请求顶层字段**(不在 generationConfig 里),传创建缓存时返回的完整资源名:

```java
ChatRequest.builder().model("gemini-2.5-flash")
        .cachedContent("cachedContents/qvk6m3...")   // 完整名,不只是 id
        .contents(contents).build();
```

缓存的创建与删除走独立的 `v1beta/cachedContents` 端点,SDK 不封装;命中数在 `usageMetadata.cachedContentTokenCount`,`Usage` 本来就有。实测一次调用报 3302 缓存 / 3307 prompt。v1alpha 与 v1beta 路径都能用。

**刻意没加的四个字段。** discovery 里还列着 `enableAffectiveDialog`、`translationConfig`、`audioTranscriptionConfig`、`responseFormat`,但实测:第一个在可用模型上**直接 400**;后三个被接受却**观察不到任何效果**(属于 Live API 或 generateContent 够不到的模态)。加上等于交付无法验证的字段,所以没加 —— `GeminiGenerationConfigLiveSmokeTest` 里有一条测试把这四个的当前行为钉住了,哪天上游变了会红。

### OpenAI:max_completion_tokens 与缓存请求参数(2.6.0)

**`maxTokens` 在当前模型上已经不能用了。** 实测 gpt-5.5 与 gpt-5.6-terra 都答 `Unsupported parameter: 'max_tokens' is not supported with this model. Use 'max_completion_tokens' instead`,两个同时发也拒。也就是说 2.6.0 之前 SDK 在这些模型上**根本没法限制输出长度** —— 唯一有的那个字段必然报错。

```java
ChatRequest.builder().model("gpt-5.6-terra")
        .maxCompletionTokens(16)     // 当前模型用这个
        .messages(messages).build();
```

`maxTokens` 保留给仍需要它的老模型,两者互不干扰,但别同时发。

**缓存请求参数**:

```java
.promptCacheKey("tenant-A")                                   // 按租户分隔缓存
.promptCacheOptions(PromptCacheOptions.builder()
        .mode("implicit").ttl("30m").build())
```

`promptCacheKey` 实测**真的分隔缓存**而不只是打标签:同一段 prompt,key `tenant-A` 第一次写入 1812、第二次命中 1812;key `tenant-B` 发完全相同的字节却又重新写入 1812、命中 0。

`promptCacheOptions` 有两个坑:

- **按模型门控**:gpt-5.5 答 `prompt_cache_options is not supported on this model`,要 gpt-5.6+
- **`ttl` 只接受 `"30m"`**:传 `12h` 答 `Supported values are: '30m'`。官方缓存指南里那个 12 小时档(写入 2.5×)是 Responses API 的
- **没有 `prewarm` 字段**:指南里有,这个端点答 `Unknown parameter: 'prompt_cache_options.prewarm'`,所以 SDK 不建模它

`mode` 取 `implicit` 或 `explicit`。实测 `explicit` 下既不读也不写 —— 显式模式要有断点标记才会缓存。

### Claude:回合级 system 消息 clear_at(2.6.0,beta)

```java
ChatRequest.builder()
        .model("claude-opus-5-5")
        .betas(Arrays.asList("mid-conversation-system-clear-at-2026-08-21"))
        .messages(Arrays.asList(
                new ChatMessage("user", "..."),
                ChatMessage.turnScopedSystem("Keep answers to one word."),  // 只渲染一轮
                new ChatMessage("assistant", "..."),
                new ChatMessage("user", "...")))
        .build();
```

`clearAt` 取 `next_user_message` 或 `never`(不设时即 never)。

**为什么要它**:每轮插一条提醒、下一轮再删掉,那是**改历史** —— 缓存从那一点起失效,而且在保留 thinking 的模型上会让后续 assistant 轮的 thinking 块全部失效。回合级消息的做法是**每轮追加一条新的、旧的留着不动**,两个问题都避开。

**两处限制**(都已实测):

| | 结果 |
|---|---|
| 带 `cache_control` | **400** `cache_control is not permitted on a turn-scoped system message` |
| 带 `output_config`(即不能和 2.4.0 的逐条 effort 合用) | **400** `output_config is not permitted on a turn-scoped system message` |
| 不带 beta 标记 | **400** `messages.N.clear_at: Extra inputs are not permitted` |
| 非法值 | **400**,报错里带枚举 |

对照:**不带 `clear_at` 的普通 mid-conversation system 消息可以带 `cache_control`**,会正常缓存(实测写入 2409 token)。

**放置位置**和其他带内容的 system 消息一样:必须紧跟 `user` 轮(或结束于服务端工具结果的 `assistant` 轮),并且后面接 `assistant` 轮或作为数组最后一条。放在 `messages[0]` 会被拒,服务端会提示改用顶层 `system` 参数。

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
