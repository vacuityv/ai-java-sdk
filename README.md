
[[中文]](https://github.com/vacuityv/ai-java-sdk/tree/develop) [[English]](https://github.com/vacuityv/ai-java-sdk/blob/develop/README-eng.md)

# AI-Java-Sdk

为使用各大AI厂商提供的API创建的java sdk，目前支持Google Gemini 和 Claude AI 以及 openai的chat部分功能。

## 支持的 Claude api
- [对话](https://docs.anthropic.com/claude/reference/messages_post)
- [流式对话](https://docs.anthropic.com/claude/reference/messages-streaming)

## 支持的 Google Gemini
- [对话](https://ai.google.dev/tutorials/rest_quickstart)
- [流式对话](https://ai.google.dev/tutorials/rest_quickstart)

## 支持的 openAI

> 支持函数调用，请参考 OpenaiTest（函数部分实现大量参考了 https://github.com/TheoKanning/openai-java ）

- [对话](https://platform.openai.com/docs/api-reference/chat/create)
- [流式对话](https://platform.openai.com/docs/api-reference/chat/streaming)



## Importing

### Maven
```xml
<dependency>
    <groupId>me.vacuity.ai.sdk</groupId>
    <artifactId>ai-java-sdk</artifactId>
    <version>1.4.0</version>       
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

你可以在 CludeTest 和 GeminiTest 以及 OpenaiTest 查看代码示例

## FAQ
### 支持什么模型
目前支持Claude ai 和 Google Gemini 以及 Openai 的部分模型

### openai 还有很多其他功能，这个sdk不支持吗
openai 目前在github上都有对应的sdk支持（比如：https://github.com/TheoKanning/openai-java ），所以不是紧急的需求，也许会在未来支持

## License
Published under the MIT License

## 如果您有余力，欢迎贡献代码 or Money

<img width="200" height="200" src="data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAkwAAAJMAQMAAAAyqmuAAAAABlBMVEX///8Awl9X2irXAAAC7UlEQVR42u2dUY4iMQwFuUHuf8vcgNUm+7CJ+WAR1rjHVYpaScPUTx7jJqThBgAAAAAAAJ25P5gf/Elf1fh74t/D4zY1nGMN90MDVeEZLKTS8G2VPRPVTuawcCque4iq/AwWUe0X5vsq/Ulr1VBFGBbOzbTygarwDKK6vkpRdBdmOq4hquoziOraKl2eKZC7v4+6ZkNVeQZLqWpeIV9BtVM6nzqqFKiuMIM1VDVXisqrxu2NhqrwDNZRwWeMfdxNNcLW8Pd5VAAZ+A/LLKvq+9D6ktFUBfBNhv+0WiH0w6HhPrrzs5sKIANl0kfRliysXqhvKe2nAsjANo0Iv31L7SgTWuRvpgLIwKrDy6GaWPnsqgLIYNjRVYRjP4nf5+zfVTVTAeQwY71wOdQTFGB7tKMKIAEl81ia0FBYON3KYT8VQAb+376OanZel2ePqtFSBZCDT52/m1WFw5cGn96OKoAMtP5whvP1aoZtJumoAvgmSlooFmLEz8vc87upAHJQIVB1UNMOk2O/pQ1nQxXA1xmvtmxpeG6eXEcLcDcVQAb+TVC8m0BDX0c2s6EKIAO7Z2ehjo6ruUd9jZjdVAAp+DVAHX1o41c9WGLbqQAyUBrV8XfxDAunlYZHpeinAshgjphAa36v17TcNlUBJDGPjc3qhC9/2EO1liqAL2N364SU6uRGHb+02EwFkMHr6zQ9FNftn4bNVAAJKIdxYVDrEjHGViaaqQAyiDskR1w51Bn/XfENVZDHfTM7qvxVmRJoxO8DlGqOfqqaM/hLVEuCKt62Y31VB6lW1WisqjmDdVT3B/Nd1X2iUkqfsip0HlXxGayimkcnEp+P6ly0X0PXOVWzs6rmDJZSrdfy26oFqrBCGN9JTaeazVU1Z/CiKoFK+J8+ifebqw1UdWcQ1VVVriis8XONON5PNVfVnME6qgWq/1KFX4X220he/oRKa1XNGSyjEqg+UllFCPe06hIOVekZ/HkVAAAAAAAANOMPsOFxJ3hFWDEAAAAASUVORK5CYII="/>


<img width="200" height="200" src="data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAkwAAAJMAQMAAAAyqmuAAAAABlBMVEX///8SeP+AhLgTAAAC8klEQVR42u2dXYrrMAxGvQPtf5faQS+j1NZILZdQbFBH52BCxknPi7/ixD/TAQAAAAAAAJ15LPSDj/RVyVArIx3HD/YnqtotWEilfnIPv7O5SjyNVyB1rHO7Kqhqt2AdlX0xb6v8I61VMgNpV/TKpNcYgqpwC6L6AyqZxSKa7HYJVe0WRPXVKktgfmmysj6jgqpwC5ZS1XxCLq9aD2mGx1WffQSq6i1YRlVzpKi8SsaNgqpwC9ZRwWfIMLxTCB2EoYIK4Agq19GK13ilR9dLUxXATlaPkDJp9fPEKlfxWbZ+KoCteD5D3mZcUz69cvYj3VQA+/GIepF59Pnrl+ns5z3NVACniNPWeQYtzmWH96mWKoDNhAmy8CqU1jaP3EGotFMBnECteE/hsYyVaeCipQpgPzN4oRew4/vlzVZmgJupAE6QRipmbj2u6zynt6MK4ASezN9TY3HswmMcFli2UwHsJ28qf5mk9kHCfBzSTAVwgjAukfOpYxLyaZcaqgBOkF6IUu9gaByy8Bv6qQD2I2E4whNrJ+/Wcc2ahiqAY6h4yYtJhiFe+USaqgA2I7MYOaJx/tprrspuKoATSNov4CEcuadI4xv9VABn0HUiabWkn3gJ09ntVAC7ieP2aeGWB/IqL8913VQARwix1DBZFveVp2FD6agCOEPewpMHEsNSrhXgriqAvYiV1/eg/y3cWnlupgI4QZgg87hqrBnh0spzMxXAfjx+MXtxDN8f0lKqm6ngHI8L7agKwcsjFa8DGq6SfqqaLfhHVCZB5bsG8tOap3SqfpLcWVWzBeuoHgu9q3ooqjibFoboffxwqhRVzRasopqV91VjoMo/wWBonLD2bmIM7ayq2YKlVPZdvq0yUPnTWt5z5/WX6Ypoc1XNFvxS1QTV6y+k5P9THTa6oqrbgqi+WOXbWkNKY8dhN6Cq2YJFVAaqD1RXAi/eb+rxmu6qmi1YQzVB9bEqbGX14jFGVbwFf8GP6gIAAAAAAMBp/gGdS7XTsQF2XgAAAABJRU5ErkJggg=="/>
