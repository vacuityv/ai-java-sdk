package me.vacuity.ai.sdk.test.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.reactivex.Flowable;
import me.vacuity.ai.sdk.openai.OpenaiClient;
import me.vacuity.ai.sdk.openai.responses.entity.Response;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseOutputContent;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseOutputItem;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseStreamEvent;
import me.vacuity.ai.sdk.openai.responses.entity.ResponseTool;
import me.vacuity.ai.sdk.openai.responses.request.ResponseRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 探针：在 SDK 层直接打 OpenAI Responses + web_search，dump 每个事件 + sources 原始 JSON
 * 用于排查 vac-chat 后端搜索为啥拿不到 sources（url=null 现象）。
 *
 * 跑法：
 *   OPENAI_API_KEY=sk-... mvn -f /Users/.../ai-java-sdk/pom.xml test \
 *     -Dtest=WebSearchProbeTest -DfailIfNoTests=false
 */
public class WebSearchProbeTest {

    private final ObjectMapper pretty = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    public void probeStreamingWebSearchSources() throws Exception {
        if (OpenaiConstant.API_KEY == null || OpenaiConstant.API_KEY.isEmpty()) {
            System.out.println("[skip] OPENAI_API_KEY env var not set");
            return;
        }
        // 强制走 OpenAI 官方，绕过任何代理，确认 sources 是否原本就缺
        OpenaiClient client = new OpenaiClient(OpenaiConstant.API_KEY, Duration.ofSeconds(60));

        ResponseRequest request = ResponseRequest.builder()
                .model("gpt-4o")
                .input("Anthropic Claude 4.7 release date and key features, summarize in one sentence")
                .tools(java.util.Collections.singletonList(ResponseTool.webSearch()))
                .include(Arrays.asList("web_search_call.action.sources"))
                .build();

        AtomicInteger n = new AtomicInteger(0);
        Flowable<ResponseStreamEvent> stream = client.streamCreateResponse(request);
        stream.blockingForEach(ev -> {
            int idx = n.incrementAndGet();
            String type = ev == null ? null : ev.getType();
            System.out.println("\n--- event #" + idx + " type=" + type + " ---");

            // dump full event JSON
            try {
                System.out.println(pretty.writeValueAsString(ev));
            } catch (Exception e) {
                System.out.println("(serialize fail: " + e.getMessage() + ")");
            }

            // 重点：如果是 web_search_call 项，把 action.sources 单独再 dump 一遍
            if (ev != null && ev.getOutputItemOrItem() != null) {
                ResponseOutputItem it = ev.getOutputItemOrItem();
                if ("web_search_call".equals(it.getType()) && it.getAction() != null) {
                    System.out.println(">> action.query   = " + it.getAction().getQuery());
                    System.out.println(">> action.sources count = "
                            + (it.getAction().getSources() == null ? "null" : it.getAction().getSources().size()));
                    if (it.getAction().getSources() != null) {
                        for (int i = 0; i < it.getAction().getSources().size(); i++) {
                            ResponseOutputItem.WebSearchSource s = it.getAction().getSources().get(i);
                            System.out.println(">>   source[" + i + "].url     = " + s.getUrl());
                            System.out.println(">>   source[" + i + "].title   = " + s.getTitle());
                            System.out.println(">>   source[" + i + "].snippet = " + s.getSnippet());
                            System.out.println(">>   source[" + i + "].extra   = " + s.getExtra());
                        }
                    }
                }
                if ("message".equals(it.getType()) && it.getContent() != null) {
                    for (int i = 0; i < it.getContent().size(); i++) {
                        ResponseOutputContent c = it.getContent().get(i);
                        System.out.println(">> content[" + i + "].type = " + (c == null ? "null" : c.getType()));
                        System.out.println(">> content[" + i + "].annotations count = "
                                + (c == null || c.getAnnotations() == null ? "null" : c.getAnnotations().size()));
                        if (c != null && c.getAnnotations() != null) {
                            for (int j = 0; j < c.getAnnotations().size(); j++) {
                                ResponseOutputContent.Annotation a = c.getAnnotations().get(j);
                                System.out.println(">>   annotation[" + j + "] type=" + a.getType()
                                        + " url=" + a.getUrl() + " title=" + a.getTitle());
                            }
                        }
                    }
                }
            }

            // response.completed 里完整 Response 也 dump 一下 output 数组
            if ("response.completed".equals(type) && ev.getResponse() != null) {
                Response full = ev.getResponse();
                System.out.println(">> completed.output items=" + (full.getOutput() == null ? 0 : full.getOutput().size()));
                if (full.getOutput() != null) {
                    for (int i = 0; i < full.getOutput().size(); i++) {
                        ResponseOutputItem it = full.getOutput().get(i);
                        System.out.println(">>   output[" + i + "].type=" + it.getType()
                                + " action=" + (it.getAction() == null ? "null" :
                                        ("query=" + it.getAction().getQuery() + " sources=" +
                                                (it.getAction().getSources() == null ? "null" :
                                                        it.getAction().getSources().size()))));
                    }
                }
            }
        });

        System.out.println("\n=== finished, total events: " + n.get() + " ===");
    }
}
