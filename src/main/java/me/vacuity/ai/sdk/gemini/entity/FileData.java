package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A file referenced by URI instead of sent inline: a Files API upload
 * ({@code https://generativelanguage.googleapis.com/v1beta/files/...}) or a
 * YouTube URL.
 *
 * @author vacuity
 * @since 2.7.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileData {

    private String fileUri;

    private String mimeType;

    private String displayName;

    public static FileData of(String fileUri, String mimeType) {
        return FileData.builder().fileUri(fileUri).mimeType(mimeType).build();
    }
}
