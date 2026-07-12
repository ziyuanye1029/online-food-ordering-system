package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    private final AliOssUtil aliOssUtil;

    public CommonController(AliOssUtil aliOssUtil) {
        this.aliOssUtil = aliOssUtil;
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传: {}", file);
        String filePath = null;
        try {
            // 获取原始文件名
            String originalFIleName = file.getOriginalFilename();
            // 截取原始文件名后缀
            String extension = originalFIleName.substring(originalFIleName.lastIndexOf("."));

            // 文件的请求路径
            String objectName = UUID.randomUUID().toString() + extension;
            filePath = aliOssUtil.upload(file.getBytes(), objectName); // 文件名用 uuid 生成, 防止文件重名导致覆盖
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.toString());
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
