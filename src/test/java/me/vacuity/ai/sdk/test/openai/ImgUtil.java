package me.vacuity.ai.sdk.test.openai;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Base64;

/**
 * @description:
 * @author: vacuity
 * @create: 2025-04-26 17:04
 **/

@Slf4j
public class ImgUtil {

    public static boolean saveBase64ImageWithBuffer(String base64String, String filePath) {
        try {
            if (base64String == null || base64String.isEmpty()) {
                return false;
            }
            // 去掉base64前缀
            if (base64String.contains(",")) {
                base64String = base64String.split(",")[1];
            }

            // 创建文件夹
            File file = new File(filePath);
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }

            // 解码base64字符串
            byte[] imageBytes = Base64.getDecoder().decode(base64String);

            // 使用缓冲流写入文件
            try (BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(filePath))) {
                bos.write(imageBytes);
                bos.flush();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
