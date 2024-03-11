
[[中文]](https://github.com/vacuityv/ai-java-sdk/tree/main) [[English]](https://github.com/vacuityv/ai-java-sdk/blob/main/README-eng.md)

# AI-Java-Sdk

为使用各大AI厂商提供的API创建的java sdk，目前支持Google Gemini 和 Claude AI，未来考虑支持openai。

## 支持的 Claude api
- [对话](https://docs.anthropic.com/claude/reference/messages_post)
- [流式对话](https://docs.anthropic.com/claude/reference/messages-streaming)

## 支持的 Google Gemini
- [对话](https://ai.google.dev/tutorials/rest_quickstart)
- [流式对话](https://ai.google.dev/tutorials/rest_quickstart)



## Importing

### Maven
```xml
   <dependency>
    <groupId>me.vacuity.ai.sdk</groupId>
    <artifactId>ai-java-sdk</artifactId>
    <version>1.2.0</version>       
   </dependency>
```

## 使用

普通对话：

```java
@Test
public void chat() {
    ClaudeClient client = new ClaudeClient(API_KEY);
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage("user", "introduce yourself pls"));
    ChatRequest request = ChatRequest.builder()
            .model("claude-3-opus-20240229")
            .messages(messages)
            .maxTokens(1024)
            .build();
    try {
        ChatResponse response = client.chat(request);
        System.out.println(response);
    } catch (VacException e) {
        if (e.getDetail() != null) {
            System.out.println(e.getDetail().getError().getMessage());
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
                .model("claude-3-opus-20240229")
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
    ObjectMapper mapper = defaultObjectMapper();
    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
    OkHttpClient httpClient = defaultClient(API_KEY, Duration.ofSeconds(60))
            .newBuilder()
            .proxy(proxy)
            .build();
    Retrofit retrofit = defaultRetrofit(httpClient, mapper, null);
    ClaudeApi api = retrofit.create(ClaudeApi.class);
    ClaudeClient client = new ClaudeClient(api);

    List<ChatMessage> messages = new ArrayList<>();
    messages.add(new ChatMessage("user", "introduce yourself pls"));
    ChatRequest request = ChatRequest.builder()
            .model("claude-3-opus-20240229")
            .messages(messages)
            .maxTokens(1024)
            .build();
    try {
        ChatResponse response = client.chat(request);
        System.out.println(response.getContent().get(0).getText());
    } catch (VacException e) {
        if (e.getDetail() != null) {
            System.out.println(e.getDetail().getError().getMessage());
        }
    }
}
```
## 其他

你可以在 CludeTest 和 GeminiTest 查看代码示例

## FAQ
### 支持什么模型
目前支持Claude ai 和 Google Gemini 的所有模型

### openai 不支持吗
openai 目前在github上都有对应的sdk支持，所以不是紧急的需求，也许会在未来支持

## License
Published under the MIT License
