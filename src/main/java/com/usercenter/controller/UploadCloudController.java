package com.usercenter.controller;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.entity.User;
import com.usercenter.exception.BusinessException;
import com.usercenter.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import static com.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

@RestController
@RequestMapping("/upload")
public class UploadCloudController {
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


    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;

    @PostMapping
    public BaseResponse<String> uploadCloudFile(@RequestParam(value = "avatar") MultipartFile avatar) {

        String oldFileName = avatar.getOriginalFilename();
        assert oldFileName != null;

        String eName = oldFileName.substring(oldFileName.lastIndexOf("."));

        String newFileName = UUID.randomUUID() + eName;

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(bucket));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        File localFile;
        try {
            localFile = File.createTempFile("temp", null);
            // FileUtil.copyFile(oldFile, localFile);
            avatar.transferTo(localFile);
            // 指定要上传到 COS 上的路径
            String key = "/" + prefix + "/" + year + "/" + month + "/" + day + "/" + newFileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
            cosclient.putObject(putObjectRequest);

            return BaseResponse.ok(path + putObjectRequest.getKey(), "上传到OOS成功");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_CLOUD_ERROR);
        } finally {
            // 关闭客户端(关闭后台线程)
            cosclient.shutdown();
        }

    }


    @PostMapping("/avatar")
    public BaseResponse<String> uploadCloudUserAvatar(@RequestParam(value = "avatar") MultipartFile avatar) {

        User user = userService.getLoginUser(request);

        String oldFileName = avatar.getOriginalFilename();
        assert oldFileName != null;

        String eName = oldFileName.substring(oldFileName.lastIndexOf("."));

        String newFileName = UUID.randomUUID() + eName;

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(bucket));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // 简单文件上传, 最大支持 5 GB, 适用于小文件上传, 建议 20 M 以下的文件使用该接口
        // 大文件上传请参照 API 文档高级 API 上传
        File localFile;
        try {
            localFile = File.createTempFile("temp", null);
            // FileUtil.copyFile(oldFile, localFile);
            avatar.transferTo(localFile);
            // 指定要上传到 COS 上的路径
            String key = "/" + prefix + "/" + year + "/" + month + "/" + day + "/" + newFileName;
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
            cosclient.putObject(putObjectRequest);

            String filePath = this.path + putObjectRequest.getKey();

            // 更新数据库的信息,注意session中的缓存信息也要更新
            user.setAvatarUrl(filePath);

            userService.update().set("avatarUrl", filePath).eq("id", user.getId()).update();

            // 更新session缓存
            request.getSession().setAttribute(USER_LOGIN_STATUS, user);

            return BaseResponse.ok(filePath, "上传到OOS成功");
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.UPLOAD_CLOUD_ERROR);
        } finally {
            // 关闭客户端(关闭后台线程)
            cosclient.shutdown();
        }

    }
}
