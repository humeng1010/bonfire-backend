package com.usercenter.controller;

import cn.hutool.core.lang.UUID;
import com.usercenter.common.BaseResponse;
import com.usercenter.common.ErrorCode;
import com.usercenter.entity.User;
import com.usercenter.exception.BusinessException;
import com.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Objects;

import static com.usercenter.constant.UserConstant.USER_LOGIN_STATUS;

@RestController
@RequestMapping("/common")
@Slf4j
@Api(tags = "文件上传下载接口")
// @CrossOrigin // 上线通过 nginx 进行反向代理解决跨域
public class UploadController {


    @Value("${upload-img.path}")
    private String basePath;

    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;


    /**
     * 文件上传
     *
     * @param avatar
     * @return
     */
    @ApiImplicitParam(name = "avatar", value = "文件", required = true)
    @ApiOperation(value = "上传文件")
    @PostMapping("/upload")
    public BaseResponse<String> uploadAvatar(MultipartFile avatar) {
        try {
//            防止目录不存在
            File file1 = new File(basePath);
            if (!file1.exists()) {
                boolean mkdirs = file1.mkdirs();
                if (!mkdirs)
                    throw new BusinessException(50008, "服务器创建文件夹失败", "请检查路径是否正确");
            }
            String uuid = UUID.randomUUID().toString();
            String originalFilename = avatar.getOriginalFilename();
            log.info("文件的原始名称:{}", originalFilename);
            assert originalFilename != null;
            // 获取文件后缀 带 .
            String substring = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = uuid + substring;

            String filePath = basePath + fileName;
            // 从session中获取当前的用户
            User user = (User) request.getSession().getAttribute(USER_LOGIN_STATUS);
            // 如果session中是null则抛出未登录异常 返回给前端
            if (Objects.isNull(user)) throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
            // 更新数据库的信息,注意session中的缓存信息也要更新
            user.setAvatarUrl(fileName);
            userService.updateById(user);
            // 更新session缓存
            request.getSession().setAttribute(USER_LOGIN_STATUS, user);
            // 文件转储
            avatar.transferTo(new File(filePath));
            return BaseResponse.ok(fileName, "上传文件成功");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 文件下载
     *
     * @param name     文件名称
     * @param response 响应对象
     */
    @ApiImplicitParam(name = "name", value = "文件名称", required = true)
    @ApiOperation("文件下载")
    @GetMapping("/download")
    public void download(@RequestParam String name, HttpServletResponse response) {
        try (
                // 读取文件
                FileInputStream fis = new FileInputStream(basePath + name);
                // 文件读取缓冲流
                BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            response.reset();
            // 获取响应输出流
            ServletOutputStream outputStream = response.getOutputStream();
            // 继续包装成缓冲流 提高文件传输速率
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            response.setContentType("image/jpeg");

            // 定义字节数组 1MB 相当于一个桶,用于不断读取数据和写出数据
            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) != -1) {
                // 把读取的字节数组写出去
                bufferedOutputStream.write(buffer, 0, len);
                bufferedOutputStream.flush();
            }

            // 这里可以不用关闭释放资源,他会根据请求和响应的结束而被结束掉
            // bufferedOutputStream.close();
            // outputStream.close();
        } catch (Exception e) {
            throw new BusinessException(50009, e.getMessage(), "读取文件异常");
        }
    }

}
