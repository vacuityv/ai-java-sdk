[[中文]](https://github.com/vacuityv/ai-java-sdk/tree/develop) [[English]](https://github.com/vacuityv/ai-java-sdk/blob/develop/README-eng.md)

# AI-Java-Sdk

This is a Java SDK created for utilizing APIs provided by various AI companies. Currently, it supports Claude AI and Google gemini and part of OpenAI.

## Supported Claude APIs
- [Chat](https://docs.anthropic.com/claude/reference/messages_post)
- [Streaming Chat](https://docs.anthropic.com/claude/reference/messages-streaming)

## Supported Google Gemini
- [Chat](https://ai.google.dev/tutorials/rest_quickstart)
- [Streaming Chat](https://ai.google.dev/tutorials/rest_quickstart)

## Supported openAI

> Support function calls, please refer to OpenaiTest (a large part of the function implementation is based on https://github.com/TheoKanning/openai-java).

- [Chat](https://platform.openai.com/docs/api-reference/chat/create)
- [Streaming Chat](https://platform.openai.com/docs/api-reference/chat/streaming)




## Importing

### Maven
```xml
<dependency>
    <groupId>me.vacuity.me.ai.sdk</groupId>
    <artifactId>ai-java-sdk</artifactId>
    <version>1.4.0</version>       
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
            .model("gpt-4-vision-preview")
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

You can view code examples in CludeTest and GeminiTest and OpenaiTest.

## FAQ
### What models are supported?
Currently, all models of Claude AI and part of gemini and openai are supported.

### Are there many other functions of OpenAI that this SDK does not support?
OpenAI currently has corresponding SDK support on GitHub (such as: https://github.com/TheoKanning/openai-java), so it is not an urgent need, perhaps it will be supported in the future.

## License
Published under the MIT License