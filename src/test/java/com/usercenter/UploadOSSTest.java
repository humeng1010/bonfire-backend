package com.usercenter;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import jodd.io.FileUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;


@SpringBootTest
public class UploadOSSTest {

    @Value("${upload.tengxun.SecretId}")
    private String accessKey;
    @Value("${upload.tengxun.SecretKey}")
    private String secretKey;
    @Value("${upload.tengxun.bucket}")
    private String bucket;
    @Value("${upload.tengxun.bucketName}")
    private String bucketName;
    @Value("${upload.tengxun.path}")
    private String path;
    @Value("${upload.tengxun.prefix}")
    private String prefix;

    @Test
    public void testUpload() {

        // String oldFileName = file.getOriginalFilename();
        File oldFile = new File("/Users/humeng/Pictures/avatar/Snipaste_2023-07-16_13-28-38.png");
        String oldFileName = oldFile.getName();

        String eName = oldFileName.substring(oldFileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + eName;
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(bucket));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式

        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        File localFile = null;
        try {
            localFile = File.createTempFile("temp", null);
            FileUtil.copyFile(oldFile, localFile);

            // 指定要上传到 COS 上的路径
            String key = "/" + prefix + "/" + year + "/" + month + "/" + day + "/" + newFileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
            PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
            // return new UploadMsg(1, "上传成功", this.path + putObjectRequest.getKey());
        } catch (IOException e) {
            // return new UploadMsg(-1, e.getMessage(), null);
        } finally {
            // 关闭客户端(关闭后台线程)
            cosclient.shutdown();
        }


    }

}
