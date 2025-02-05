package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-02-05 12:34
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSearchRetrieval {
    
    @JsonProperty("dynamic_retrieval_config")
    private DynamicRetrievalConfig dynamicRetrievalConfig;
}
