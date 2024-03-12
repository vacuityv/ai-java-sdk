[[中文]](https://github.com/vacuityv/ai-java-sdk/tree/develop) [[English]](https://github.com/vacuityv/ai-java-sdk/blob/develop/README-eng.md)

# AI-Java-Sdk

This is a Java SDK created for utilizing APIs provided by various AI companies. Currently, it supports Claude AI and Google gemini, with future plans to support OpenAI.

## Supported Claude APIs
- [Chat](https://docs.anthropic.com/claude/reference/messages_post)
- [Streaming Chat](https://docs.anthropic.com/claude/reference/messages-streaming)

## Supported Google Gemini
- [Chat](https://ai.google.dev/tutorials/rest_quickstart)
- [Streaming Chat](https://ai.google.dev/tutorials/rest_quickstart)



## Importing

### Maven
```xml
   <dependency>
    <groupId>me.vacuity.me.ai.sdk</groupId>
    <artifactId>ai-java-sdk</artifactId>
    <version>1.2.0</version>       
   </dependency>
```

## Usage

For regular chat:

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

For streaming chat:

```java
@Test
    public void streamChat() {
        ClaudeClient client = new ClaudeClient(API_KEY);
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", "Why did Lu Xun hit Zhou Shuren"));
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
## Additional Information

You can view code examples in CludeTest and GeminiTest.

## FAQ
### What models are supported?
Currently, all models of Claude AI and part of gemini are supported.

### Is OpenAI not supported?
OpenAI currently have their respective SDKs available on GitHub, so their support is not an urgent necessity but may be considered in the future.

## License
Published under the MIT License