[[中文]](https://github.com/vacuityv/ai-java-sdk/tree/develop) [[English]](https://github.com/vacuityv/ai-java-sdk/blob/develop/README-eng.md)

# AI-Java-Sdk

> Welcome to experience the interactive website connecting individuals with major AI
> vendors: [AI-CHAT](https://chat.vacuity.me/)

This is a Java SDK created for utilizing APIs provided by various AI companies. It currently supports Anthropic Claude,
Google Gemini and OpenAI, along with OpenAI-compatible vendors such as xAI Grok and DeepSeek.

It targets Java 8 and builds on JDK 8 through 23.

## Supported Claude APIs

- [Chat (support function)](https://docs.anthropic.com/claude/reference/messages_post)
- [Streaming Chat (support function)](https://docs.anthropic.com/claude/reference/messages-streaming)
- Tool choice (`tool_choice`: force a named tool / force any tool / disable tools / disable parallel calls)
- [Prompt caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching) (breakpoints can be placed on
  the system prompt, message content blocks and tool definitions)
- [Adaptive thinking and effort](https://platform.claude.com/docs/en/build-with-claude/adaptive-thinking)
- [Structured outputs](https://platform.claude.com/docs/en/build-with-claude/structured-outputs) (`output_config.format`)
- Refusal details (`stop_details`), task budgets, context editing, MCP servers, fast mode and more
- Per-request `anthropic-beta` header
- Thinking block prefix validation (`block_binding`) and the thinking-token breakdown (`output_tokens_details`)

## Supported Google Gemini

- [Chat (support function)](https://ai.google.dev/tutorials/rest_quickstart)
- [Streaming Chat (support function)](https://ai.google.dev/tutorials/rest_quickstart)
- [Veo video generation](https://ai.google.dev/gemini-api/docs/video)
- Built-in tools alongside function declarations (`toolConfig.includeServerSideToolInvocations`)

## Supported openAI

> Support function calls, please refer to OpenaiTest (a large part of the function implementation is based
> on https://github.com/TheoKanning/openai-java).

- [Chat (support function)](https://platform.openai.com/docs/api-reference/chat/create)
- [Streaming Chat (support function)](https://platform.openai.com/docs/api-reference/chat/streaming)
- [Responses API (include stream)](https://developers.openai.com/api/reference/responses/overview)
- [File](https://platform.openai.com/docs/api-reference/files)
- [Assistant (include stream)](https://platform.openai.com/docs/api-reference/assistants)
- [Image](https://platform.openai.com/docs/api-reference/images) (generate / edit / variation, **generate and edit support streaming**)
- [Batch](https://platform.openai.com/docs/api-reference/batch)
- [Realtime session](https://platform.openai.com/docs/api-reference/realtime-sessions)
- [Video (Sora)](https://platform.openai.com/docs/api-reference/videos)
- [Decisions API](https://developers.openai.com/api/docs/guides/decisions) (structured judgements, gpt-6-luna only)

## Importing

### Maven

```xml
<dependency>
    <groupId>me.vacuity.ai.sdk</groupId>
    <artifactId>ai-java-sdk</artifactId>
    <version>${version}</version>       
</dependency>
```

You can get the version here：[Maven Central](https://central.sonatype.com/artifact/me.vacuity.ai.sdk/ai-java-sdk)

## Usage

For regular chat:

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
        // detail carries the raw server error object; for Claude it is a ChatResponseError
        if (e.getDetail() instanceof ChatResponseError) {
            ChatResponseError detail = (ChatResponseError) e.getDetail();
            System.out.println(detail.getError().getMessage());
        }
    }
}
```

For streaming chat:

```java

@Test
public void streamChat() {
    ClaudeClient client = new ClaudeClient(API_KEY);
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage("user", "Why did Lu Xun hit Zhou Shuren"));
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

### Forcing a specific tool

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .messages(messages)
        .maxTokens(1024)
        .tools(tools)
        .toolChoice(ToolChoice.tool("get_weather"))
        .build();
```

`ToolChoice` also provides `auto()` (the default — the model decides), `any()` (the model must call some tool) and
`none()` (tools are disabled). Call `setDisableParallelToolUse(true)` to allow at most one tool call per turn.

### Prompt caching

Cache reads cost roughly 0.1x the base input price. Put stable content (system prompt, tool definitions) first with a
cache breakpoint, and anything that changes per request after it.

```java
ChatMessageContent system = new ChatMessageContent();
system.setType("text");
system.setText(longSystemPrompt);
system.setCacheControl(CacheControl.ephemeral("1h"));   // or ephemeral() for the default 5m

ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .system(Collections.singletonList(system))
        .messages(messages)
        .maxTokens(1024)
        .build();

ChatResponse response = client.chat(request);
// Verify the cache is actually hit. If this stays 0 across identical requests,
// something in the prefix is changing.
System.out.println(response.getUsage().getCacheReadInputTokens());
```

Breakpoints can also be set on `ChatMessageContent` (message content blocks), `ChatFunction` (tool definitions), or on
`ChatRequest` itself (applied to the last cacheable block automatically).

### Thinking and effort

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .messages(messages)
        .maxTokens(8192)
        .thinking(Thinking.adaptive())                                // the model decides how much to think
        .outputConfig(OutputConfig.builder().effort("high").build())   // low / medium / high / xhigh / max
        .build();
```

Use `Thinking.adaptive("summarized")` to ask for a reasoning summary. The default is `"omitted"`, which still returns
thinking blocks but with empty text. Whether a summary is produced is decided server-side and is not guaranteed.

`Thinking.budgetTokens` is deprecated: it returns a 400 on Opus 4.7 and later, Opus 5, Sonnet 5 and others — use
`effort` instead. The same applies to `temperature` / `topP` / `topK`, which those models no longer accept.

### Using beta features

```java
ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .messages(messages)
        .maxTokens(1024)
        .betas(Arrays.asList("context-management-2025-06-27"))
        .build();
```

Multiple flags are joined with commas into the `anthropic-beta` header. Leaving `betas` unset keeps the previous
default behaviour.

### Streaming image generation

Generating an image usually takes tens of seconds. Streaming delivers partial images before the final one — measured here at roughly 5 seconds earlier for the first viewable image.

```java
CreateImageRequest request = CreateImageRequest.builder()
        .model("gpt-image-2.6-sunburst") // streaming is gpt-image only; dall-e does not support it
        .prompt("a simple orange fox, flat vector style")
        .size("1024x1024")
        .partialImages(2)              // upper bound on partial images, 0-3
        .build();

client.streamCreateImage(request).blockingSubscribe(event -> {
    if (event.isCompleted()) {
        save(event.getB64Json());                 // the final image
        log(event.getUsage().getTotalTokens());   // usage only appears on the completed event
    } else {
        preview(event.getB64Json());              // a partial image
    }
});
```

Editing streams too:

```java
client.streamEditImage(request, imageFile, maskFile)
```

**Every event carries a complete, standalone image rather than an incremental chunk.** This is the opposite of text streaming: replace the whole image (swap the `img` src) instead of appending.

Three things to watch:

- `partialImages` is an upper bound, not a guarantee — the server may send one or none, so don't assume a count
- `usage` is only present on the completed event; it is null on partials
- The payload is large: one low-quality 1024x1024 generation totalled about 3.6MB of base64 across its events

The streaming methods need the internal OkHttpClient, so they **cannot be used with the `new OpenaiClient(OpenaiApi)` constructor** — that throws `IllegalStateException`.

> `gpt-image-1` has been retired upstream — it now answers `The model 'gpt-image-1' does not exist.` — so the examples pin `gpt-image-2.6-sunburst`.

### GPT-Image-2.6: GIF, SVG and markup edits

`gpt-image-2.6-sunburst` and `gpt-image-2.6-flare` add two output formats and two new controls.

**Animated GIF**

```java
ImageResponse gif = client.createImage(CreateImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("a small waving cartoon cat")
        .outputFormat("gif")
        .frameCount(4)                 // 2-16, default 9
        .frameDurationMs(150)          // min 20ms, no upper bound, default 200
        .complexity("low")             // low / medium / high, default high
        .size("512x512")
        .build());

gif.getOutputFormat();                 // "gif" — the server echoes the settings used
gif.getData().get(0).getGenerationId();
```

Frames are rendered as a single sprite sheet before being assembled, so `frameCount` and `size` are not independent: the sheet's longest edge must stay within 3840px. 4 frames at `1024x1024` is rejected (`No supported sheet fits 4 GIF frames`), while the same 4 frames at `512x512` are fine. Lower the size, not the frame count, when that error comes back.

**SVG**

```java
ImageResponse svg = client.createImage(CreateImageRequest.builder()
        .model("gpt-image-2.6-flare")
        .prompt("a flat icon of a bicycle")
        .outputFormat("svg")
        .svg(SvgOptions.model("gpt-6.1-terra"))   // required, 400 otherwise
        .build());

// b64_json holds base64-encoded SVG source text, not a bitmap
String source = new String(Base64.getDecoder()
        .decode(svg.getData().get(0).getB64Json()), StandardCharsets.UTF_8);

svg.getSvgUsage().getModel();          // "gpt-6.1-terra"
svg.getSvgUsage().getTotalTokens();    // billed at the authoring model's own rates
svg.getUsage().getTotalTokens();       // a separate bill — do not add the two together
```

Omitting `svg.model` returns `missing_required_parameter`. SVG is generations-only and requires `n` = 1.

**Markup edits**

`markup` is a PNG matching the input image's dimensions and containing transparent pixels, used to circle the region to change — more flexible than `mask`. It lives on the request object rather than as another method parameter, so the existing `editImage` overloads keep their signatures.

```java
client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("paint the circled area bright yellow")
        .markup(annotationPng)         // same size as the first input image, with transparency
        .complexity("low")
        .build(), sourceImage, (File) null);
```

`markup` and `mask` are mutually exclusive — the server returns `mutually_exclusive_parameters`, and the SDK throws `IllegalArgumentException` before spending the round trip.

**Also**

- `safetyIdentifier` is the newer name for `user`; both still work, may be sent together, and are echoed on the response
- The response now echoes `background` / `outputFormat` / `quality` / `size`, which is how to see what defaults the server picked
- `usage.outputTokensDetails` completes the image/text token split on the output side

> 2.1.0's docs gave `frameCount` as 2-64 and `frameDurationMs` as 20-2000ms. Both were wrong. Per the official reference and live testing: `frameCount` is **2-16** on both endpoints, and `frameDurationMs` has a minimum of 20 and **no upper bound** (the encoder rounds to 10ms and caps the encoded delay at 655350ms per frame; 5000 is accepted as sent). `complexity` also has no fixed default — omitting it uses the image model's own configured default.

### GIF edits (2.2.0)

Edits produce animated GIFs too, with the same framing fields:

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

That `512x512` is not interchangeable with a plain edit: a PNG edit at the same size is refused as `below the current minimum pixel budget`, because for GIF the budget is measured on the sprite sheet rather than one frame. GIF edits support neither `mask`, `stream`, `partialImages` nor `outputCompression`.

**SVG edits work as of 2.2.1.** They were refused outright until early November 2026 (`Invalid value: 'svg'. Supported values are: 'png', 'webp', and 'jpeg'.`) and now work on gpt-image-2.6 — a change the SDK's own live tests caught. Like SVG generation they need `svg.model` and `n=1` and rule out streaming; unlike it, a mask is accepted:

```java
ImageResponse svg = client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-flare")
        .prompt("redraw this as a flat vector icon")
        .outputFormat("svg")
        .svg(SvgOptions.of("gpt-6.1-terra", "low"))
        .build(), sourceImage, (File) null);
```

### Reasoning for the SVG author model (2.2.0)

`svg.reasoning.effort` controls how hard the authoring model thinks, which is a different thing from `complexity` (which controls the reference image):

```java
ImageResponse svg = client.createImage(CreateImageRequest.builder()
        .model("gpt-image-2.6-flare")
        .prompt("a flat icon of a bicycle")
        .outputFormat("svg")
        .svg(SvgOptions.of("gpt-6.1-terra", "low"))   // model + reasoning.effort
        .build());
```

`effort` is one of `none`, `minimal`, `low`, `medium`, `high`, `xhigh`, `max`, and omitting it uses the author model's own default. It moves the bill: for one prompt, `svgUsage.reasoningTokens` measured 501-1226 with the effort unset and 57 at `low`.

### Editing an image the API already holds (2.2.0)

The edit endpoint also has a JSON form, where input images are **referenced** by `file_id` or URL instead of uploaded as bytes. When the same image is edited repeatedly, uploading once beats resending it every time — and the multipart form cannot do this at all.

```java
// upload once
OpenaiFile uploaded = client.uploadFile("vision", "/path/to/mug.png");

// then reference it by id, with no bytes on the wire
ImageResponse result = client.editImage(EditImageRequest.builder()
        .model("gpt-image-2.6-sunburst")
        .prompt("make the mug red")
        .images(Collections.singletonList(ImageReference.fileId(uploaded.getId())))
        .quality("low")
        .size("1024x1024")
        .build());
```

An `ImageReference` carries exactly one of `fileId(..)` or `imageUrl(..)` (a `data:image/png;base64,...` URL works, up to 20MB encoded). `images` holds up to 16. On this path the mask and markup layer are references too, not file fields:

```java
client.editImage(EditImageRequest.builder()
        .prompt("paint the circled area yellow")
        .images(Collections.singletonList(ImageReference.imageUrl(sourceDataUrl)))
        .markupReference(ImageReference.imageUrl(markupDataUrl))
        .build());
```

Streaming works as well. Note that edits emit `image_edit.*` events rather than `image_generation.*` — `isCompleted()` covers both:

```java
client.streamEditImage(request).blockingSubscribe(event -> { ... });
```

**The two paths do not mix.** `editImage(request)` sends JSON and cannot carry the `markup` file field; the overloads taking `java.io.File` send multipart and cannot carry `images` / `maskReference` / `markupReference`. Setting a field the chosen transport cannot send throws `IllegalArgumentException` rather than dropping it silently — silent dropping is how `moderation` stayed dead from 1.9.17 to 1.11.0.

### On client-side validation

The SDK fails fast in exactly two places: `svg` output without an `svg.model`, and a mask given together with a markup layer. Every other format restriction — GIF rules out streaming, SVG requires `n=1`, a transparent background rules out jpeg — is left to the server.

During 2.2.0 all of those were mirrored client-side and then removed. Mirroring a rule means the SDK keeps enforcing it after upstream relaxes it, and it costs the live tests their ability to notice that happening. Those rules are now pinned by `OpenaiImageLiveSmokeTest` against the live API instead: a failure there does not mean the SDK broke, it means the API changed.

### Thinking block validation in multi-turn Claude conversations

For accounts created on or after 2026-08-31, the API enforces a prefix check on thinking blocks by default: if you change the system prompt, the tools, or earlier messages, the block becomes invalid and the request returns a 400. Anthropic states that later models will enforce this for every account.

If your multi-turn code rebuilds history, you can degrade instead of failing:

```java
Thinking thinking = Thinking.adaptive();
thinking.setBlockBinding(BlockBinding.dropBlock());   // the default is error()

ChatRequest request = ChatRequest.builder()
        .model("claude-opus-5")
        .thinking(thinking)
        .betas(Arrays.asList("thinking-binding-controls-2026-08-01"))
        .messages(messages)
        .maxTokens(4096)
        .build();
```

Dropped blocks are listed in the response's `getInputTransformations()`. Separately, `usage.getOutputTokensDetails().getThinkingTokens()` reports how many of the billed output tokens went to internal reasoning.

### Decisions API (structured judgements)

`POST /v1/decisions` returns a typed choice with probabilities instead of free text — useful for classification, routing, and picking an agent's next step. Only **`gpt-6-luna`** supports it, and **only input tokens are billed** ($0.10/1M); output tokens and cache reads/writes are free.

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

The three question types:

| Type | Extra fields | Returns |
|---|---|---|
| `predicate` | none | `probability` that the statement holds |
| `choice` | `choices[]` | `choice` + `confidence` + the full distribution |
| `score` | `levels[]` (low to high) | `score` + `confidence` + per-level probabilities |

**`score` is a probability-weighted average of the level indexes, not a level index.** With low/medium/high, 1.94 means "almost certainly high, with slight hesitation". Compare it against a threshold directly — it carries more information than a single label. The order of `levels` is therefore load-bearing: pass them lowest to highest.

Things to watch:

- Check `isRefusal()` before reading any value field; on a refusal they are all null
- Independent questions can share one `questions` array; dependent decisions need separate requests
- Image input must be an inline base64 data URL — hosted http(s) URLs and `file_id` are rejected
- `usage` reports `output_tokens` and `reasoning_tokens`, but the docs state these are not billed

### Combining Gemini built-in tools with function declarations

On `gemini-4-argon`, a request carrying both function declarations and `googleSearch` must enable `toolConfig.includeServerSideToolInvocations` or the API returns a 400:

> Please enable tool_config.include_server_side_tool_invocations to use Built-in tools with Function calling.

```java
ChatRequest request = ChatRequest.builder()
        .model("gemini-4-argon")
        .contents(contents)
        .tools(Collections.singletonList(
                Tool.functionsWithGoogleSearch(functions)))   // declarations + built-in search
        .toolConfig(ToolConfig.includeServerSideToolInvocations())
        .build();
```

Use `Tool.googleSearch()` for search alone. When `toolConfig` is unset the field is omitted from the body entirely, so older models are unaffected.

**Enabling it adds two part types to the response:**

```java
for (ChatMessageContentPart part : candidate.getContent().getParts()) {
    if (part.getToolCall() != null) {
        // a tool the model ran server-side, e.g. GOOGLE_SEARCH_WEB with its queries
    } else if (part.getToolResponse() != null) {
        // that call's result
    }
}
```

Both part types **also carry `thoughtSignature`**. Gemini 3/4 expect the previous turn's model parts to be replayed verbatim, so `ChatMessageContentPart` now keeps a catch-all (`getExtra()`): fields the server returns but this SDK has not modelled are preserved and written back out unchanged instead of being silently dropped.

### Cache tokens on OpenAI-compatible chat completions (2.2.1 onwards)

`Usage` mirrors the fields each vendor documents, under that vendor's own wire name, with no normalising layer: hit counts come in two shapes, both are carried, and the SDK neither picks one for you nor does the arithmetic:

```java
Usage u = client.chat(request).getUsage();

u.getPromptTokensDetails().getCachedTokens();              // OpenAI / Bailian / DeepSeek
u.getPromptTokensDetails().getCacheWriteTokens();          // OpenAI, gpt-5.6 onwards
u.getPromptTokensDetails().getCacheCreationInputTokens();  // Bailian's write count
u.getPromptCacheHitTokens();                               // DeepSeek's flat shape
u.getPromptCacheMissTokens();
u.getCompletionTokensDetails().getReasoningTokens();
```

| Field | Used by | Verified against |
|---|---|---|
| `promptTokensDetails.cachedTokens` | OpenAI, Bailian, DeepSeek | all three vendors' docs + live OpenAI |
| `promptTokensDetails.cacheWriteTokens` | OpenAI, gpt-5.6 and later | live gpt-5.6-terra |
| `promptTokensDetails.cacheCreationInputTokens` | Alibaba Bailian compatible mode | Bailian docs (**not live-verified**) |
| `promptCacheHitTokens` / `promptCacheMissTokens` | DeepSeek's flat shape | DeepSeek docs (**not live-verified**) |
| `completionTokensDetails` | `reasoningTokens` / `audioTokens` / `acceptedPredictionTokens` / `rejectedPredictionTokens` | live OpenAI |

**Containment follows each vendor's own docs and differs between them — bill by the one you are calling:**

- **OpenAI**: `promptTokens` **is** the total; `cachedTokens` and `cacheWriteTokens` are disjoint subsets of it. Measured on gpt-5.6-terra, same prompt twice, `promptTokens` 2655 both times:

  | | ordinary | cached | cacheWrite |
  |---|---|---|---|
  | first call (write) | 3 | 0 | 2652 |
  | second call (read) | 3 | 2652 | 0 |

  The three are priced differently, so a bill has three terms: `ordinary * inputRate + cacheWrite * inputRate * writeMultiplier + cached * inputRate * readMultiplier`. On gpt-5.6+ writes are 1.25x (2.5x for the 12-hour lifetime) and reads 0.1x (0.05x for Sol and Terra); earlier models carry no write charge.

- **DeepSeek**: its docs state `prompt_tokens = prompt_cache_hit_tokens + prompt_cache_miss_tokens`, and describe `cached_tokens` as "Same as `prompt_cache_hit_tokens`".

- **Alibaba Bailian**: its docs describe `cached_tokens` as "a part of" `usage.prompt_tokens`. But **they do not state how `cache_creation_input_tokens` relates to `prompt_tokens`**, and the SDK does not guess — OpenAI's equivalent is a subset, but that is OpenAI's behaviour, not a documented fact about Bailian. Check it against your own traffic before billing on it. Bailian's published multipliers: explicit cache writes 1.25x and reads 0.1x; implicit writes 1.0x and reads 0.2x.

A field that is **not returned** reads as `null`, not `0` — "this model does not report it" and "it was zero this time" are different facts, and gpt-5.5 reports no write field at all.

**Streaming has to ask for usage.** Against OpenAI, without `streamOptions.includeUsage` **no chunk carries usage at all** — not even the last one. With it set, a separate usage-only chunk is emitted and every earlier chunk carries `usage: null`:

```java
ChatRequest request = ChatRequest.builder()
        .model("gpt-5.5").messages(messages)
        .streamOptions(StreamOptions.builder().includeUsage(true).build())
        .build();

client.streamChat(request).blockingSubscribe(chunk -> {
    if (chunk.getUsage() != null) bill(chunk.getUsage());   // only the last is non-null
});
```

DeepSeek instead documents usage riding on the last content chunk, with no separate usage-only chunk. Code that bills from a stream must handle both — test `getUsage() != null` rather than counting chunks.

### ⚠️ Claude's cache arithmetic is the opposite of OpenAI's

The easiest thing to get wrong when billing across vendors. The SDK does not reconcile it for you, so this difference is yours to handle:

| | How to get the total prompt tokens |
|---|---|
| OpenAI-compatible | `promptTokens` **is** the total; cache counts are subsets → **subtract** |
| Claude | `inputTokens` is the uncached **remainder** → **add** |

```java
// OpenAI-compatible: promptTokens is the total
openaiUsage.getPromptTokens();

// Claude: do NOT treat inputTokens as the total
claudeUsage.getInputTokens()
        + claudeUsage.getCacheCreationInputTokens()
        + claudeUsage.getCacheReadInputTokens();
```

Measured on claude-opus-5-5 with a ~4.8k-token system prompt under `cache_control`, same request twice:

| | inputTokens | cacheCreation | cacheRead | total |
|---|---|---|---|---|
| first call (write) | 13 | 4763 | 0 | 4776 |
| second call (read) | 13 | 0 | 4763 | 4776 |

**`inputTokens` was 13 both times.** Treating it as a total the way OpenAI's field works would bill this request for 13 tokens instead of 4776.

### Claude cache writes are priced by lifetime (2.3.0)

Claude's write rate depends on the TTL — **1.25x for 5 minutes, 2x for 1 hour** — so `cacheCreationInputTokens` on its own cannot be priced. 2.3.0 adds the split:

```java
claudeUsage.getCacheCreation().getEphemeral5mInputTokens();   // 1.25x
claudeUsage.getCacheCreation().getEphemeral1hInputTokens();   // 2x
```

The two add up to `cacheCreationInputTokens`. Reads run about 0.1x the base input rate (0.05x on Opus 5.5 and Sonnet 5.5, 0.025x on Fable / Mythos 5.1 and later) — rates vary by model, so check your own.

`inferenceGeo` and **`speed`** were added too. `speed` is billing-relevant (fast mode is priced at roughly 2x) and reports the speed that actually served the request, which can differ from the one asked for since a request may fall back to standard. Bill what it reports.

### Gemini thinking tokens and billing

On Gemini 2.5 / 3.x / 4 the thinking tokens live in `usageMetadata.thoughtsTokenCount`. They are **not included in `candidatesTokenCount`**, yet Google bills them at the output rate — billing on `candidatesTokenCount` alone undercounts.

```java
Usage usage = response.getUsageMetadata();

usage.getCandidatesTokenCount();        // the visible answer
usage.getThoughtsTokenCount();          // thinking; null when the model did not think
usage.getBillableOutputTokenCount();    // both together, i.e. what is billed as output
```

Also filled in: `cachedContentTokenCount`, `toolUsePromptTokenCount`, `trafficType`, and the four `ModalityTokenCount` breakdowns (`promptTokensDetails` / `candidatesTokensDetails` / `cacheTokensDetails` / `toolUsePromptTokensDetails`). Absent fields stay null.

**Two things matter when billing a stream:**

1. `usageMetadata` arrives on **every chunk**, not just the last one
2. Its counters are **cumulative, not per-chunk deltas** — summing them overcounts badly

So keep the last one you see:

```java
final Usage[] last = new Usage[1];
client.streamChat(request).blockingSubscribe(chunk -> {
    if (chunk != null && chunk.getUsageMetadata() != null) {
        last[0] = chunk.getUsageMetadata();      // overwrite, never accumulate
    }
});
```

**Model behaviour differs:** `gemini-4-argon` does not think unless `thinkingLevel` (low / medium / high) is set — `includeThoughts` alone produces no thinking tokens. `gemini-3.8-flash` thinks by default.

### Detecting truncation on a Claude stream

The `delta` of a `message_delta` event now exposes `stop_reason` and `stop_sequence`, so there is no need to infer truncation from `output_tokens >= max_tokens`:

```java
client.streamChat(request).blockingSubscribe(event -> {
    if ("message_delta".equals(event.getType())) {
        String reason = event.getDelta().getStopReason();   // max_tokens / end_turn / tool_use ...
        if ("max_tokens".equals(reason)) {
            // truncated
        }
    }
});
```

Content block deltas (`text_delta` / `thinking_delta` / `input_json_delta` / `signature_delta`) parse exactly as before; both fields are null on those.

### Claude API version (defaults to 2026-10-21 from 2.0.0)

From 2.0.0 the `anthropic-version` header changes from `2023-06-01` to **`2026-10-21`**, and **`temperature` / `topP` / `topK` are removed from `ChatRequest`**.

That API version no longer accepts the three parameters: sending one returns a 400 **even at its former default value** (verified: `temperature: 1.0` fails with `Extra inputs are not permitted`). Since they can no longer be sent, the fields are gone — so these call sites now fail at **compile time** rather than at runtime.

```java
// Gone in 2.0.0; this no longer compiles
ChatRequest.builder().model("claude-opus-5").temperature(0.2f)

// Just drop it
ChatRequest.builder().model("claude-opus-5").maxTokens(1024).build();
```

Per Anthropic: newer models (Claude 4.7 and later) only ever accepted the defaults, so the parameters meant nothing there; on older models a lowered `temperature` loses its effect and **no other setting replaces it**.

The version is still selectable, but note it **cannot** bring sampling back — the fields no longer exist:

```java
ClaudeClient client = new ClaudeClient(
        API_KEY, Duration.ofSeconds(60), null, ClaudeClient.LEGACY_ANTHROPIC_VERSION);
```

It is there for other cases, such as an Amazon Bedrock deployment on a model older than Claude 4.7, which `2026-10-21` does not cover.

The version's other changes do not affect this SDK: `citations` in user messages (no such field here) and the Admin API renames (not covered). But a `document` block placed directly in an `assistant` message now returns a 400 — documents belong in `user` messages.

Authentication is unchanged and still uses `x-api-key`. The official SDKs switching to `Authorization: Bearer` is internal to them; the API accepts either header.

### between_tools thinking

`Thinking.betweenTools()` turns off up-front thinking while keeping the progress updates the model writes between tool calls.

```java
ChatRequest.builder()
        .model("claude-sonnet-5-5")
        .thinking(Thinking.betweenTools())
        .tools(tools)
        .maxTokens(2048)
        .build();
```

**Claude Sonnet 5.5 only** — every other model returns a 400. It exists because Sonnet 5.5 rejects `Thinking.disabled()`, making `between_tools` its only way to reduce thinking. Limits: accepted at effort `high` or below, and it takes no other field, so `display` cannot be combined with it. With no tools in the request the response is plain text.

Check `capabilities.thinking.types.between_tools.supported` on the Models API before sending it to an untested model.

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

### Customizing the URL and Timeout

```java
ClaudeClient client = new ClaudeClient(API_KEY, Duration.ofSeconds(100), "https://example.com");
```

### Using an HTTP Proxy

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
        // detail carries the raw server error object; for Claude it is a ChatResponseError
        if (e.getDetail() instanceof ChatResponseError) {
            ChatResponseError detail = (ChatResponseError) e.getDetail();
            System.out.println(detail.getError().getMessage());
        }
    }
}
```

## Upgrading from 1.9.x to 1.10.0

`ChatRequest.getSystem()` now returns `Object` instead of `String`, so that the system prompt can be sent as text
blocks carrying a cache breakpoint.

The builder keeps a `system(String)` overload, so **existing builder code is unaffected**. Only code that assigns
`getSystem()` straight to a `String` needs a change:

```java
// before
String sys = request.getSystem();
// after
String sys = (String) request.getSystem();
```

## Additional Information

You can view code examples in ClaudeTest, GeminiTest and OpenaiTest / OpenaiAssistantTest (they need real API keys).

`src/test/java/me/vacuity/ai/sdk/test/unit/` holds the offline tests that need no credentials; `mvn test` runs only
those by default.

## FAQ

### What models are supported?

Mainstream models from Claude, Google Gemini and OpenAI, plus OpenAI-compatible vendors such as xAI Grok and DeepSeek.
Use whichever model IDs are currently live for each vendor — retired models return a 404.

### Are there many other functions of OpenAI that this SDK does not support?

OpenAI currently has corresponding SDK support on GitHub (such as: https://github.com/TheoKanning/openai-java), so it is
not an urgent need, perhaps it will be supported in the future.

## License

Published under the MIT License
