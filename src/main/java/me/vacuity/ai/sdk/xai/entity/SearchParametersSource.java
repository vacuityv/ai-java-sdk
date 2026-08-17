package me.vacuity.ai.sdk.xai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-05-22 16:57
 **/

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchParametersSource {

    private String type;

    private String country;
    
    @JsonProperty("excluded_websites")
    private List<String> excludedWebsites;
    
    @JsonProperty("safe_search")
    private Boolean safeSearch;

    @JsonProperty("max_search_results")
    private Integer maxSearchResults;

    @JsonProperty("x_handles")
    private List<String> xHandles;
    
    private List<String> links;
}
