package me.vacuity.ai.sdk.gemini.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: vacuity
 * @create: 2024-12-16 15:04
 **/

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
// 无字段类：@AllArgsConstructor 生成的即为无参构造器，再加 @NoArgsConstructor 会冲突
public class CodeExcution implements Serializable {

}
