package me.vacuity.ai.sdk.claude.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Remote MCP server the model may call tools on.
 * <p>
 * Beta — send the "mcp-client-2025-11-20" flag via ChatRequest#betas. Every
 * server declared here must also be referenced by an {@code mcp_toolset} entry
 * in the request's tools, or the request is rejected.
 *
 * @author: vacuity
 * @create: 2026-08-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class McpServer {

    /**
     * Always "url".
     */
    private String type;

    /**
     * Unique server name, referenced by mcp_toolset.mcp_server_name.
     */
    private String name;

    /**
     * The MCP server endpoint.
     */
    private String url;

    /**
     * Optional bearer token for the server.
     */
    @JsonProperty("authorization_token")
    private String authorizationToken;

    public static McpServer url(String name, String url) {
        return McpServer.builder().type("url").name(name).url(url).build();
    }
}
