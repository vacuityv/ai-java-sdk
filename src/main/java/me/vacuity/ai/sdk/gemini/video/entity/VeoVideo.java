package me.vacuity.ai.sdk.gemini.video.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @description: Video entity from Veo prediction response
 * @author: vacuity
 * @create: 2025-10-16
 **/

@Data
public class VeoVideo {

    /**
     * Base64 encoded video bytes
     */
    @JsonProperty("bytesBase64Encoded")
    private String bytesBase64Encoded;

    /**
     * Video URI for download (requires API key authentication)
     */
    private String uri;

    /**
     * Video URI (when stored in GCS)
     */
    private String gcsUri;

    /**
     * MIME type of the video
     */
    private String mimeType;
}
