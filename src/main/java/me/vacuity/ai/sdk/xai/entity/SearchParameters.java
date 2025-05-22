package me.vacuity.ai.sdk.xai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-05-22 16:57
 **/

@Data
@Builder
public class SearchParameters {

    private String mode;

    @JsonProperty("return_citations")
    private Boolean returnCitations;
    
    @JsonProperty("from_date")
    private String fromDate;
    
    @JsonProperty("to_date")
    private String toDate;

    @JsonProperty("max_search_results")
    private Integer maxSearchResults;

    private List<SearchParametersSource> sources;
}
