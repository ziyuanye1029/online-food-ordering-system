package com.sky.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(byte[] bytes, String objectName) {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKeyId, accessKeySecret)
                    .build();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType("application/octet-stream")
                            .build()
            );
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        String fileUrl = endpoint + "/" + bucketName + "/" + objectName;
        log.info("文件上传到: {}", fileUrl);
        return fileUrl;
    }
}
