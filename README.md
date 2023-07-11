# 篝火-伙伴匹配APP-后端

## 后端使用的技术

SpringBoot、MyBatis-Plus、hutool、knife4j、swagger、MySQL、Redis、Redisson、flyway、lombok

> 基于 Vue 3 + Spring Boot 2 的移动端网站，实现了匹配伙伴、按标签检索用户、推荐相似用户、组队等功能。

使用**编辑距离算法**实现了根据标签匹配最相似用户

运行项目后访问`localhost:8080/api/doc.html`可以访问由knife4j生成的api接口文档

<img src="https://blog-images-1309758663.cos.ap-nanjing.myqcloud.com/202307111654211.png" alt="image-20230711165425158" style="zoom:50%;" />



## 前端基本页面展示

| 首页推荐页面                                                 | 队伍组队页面                                                 | 我的队伍页面+匹配伙伴页面                                    | 个人信息页面                                                 |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| <img src="https://blog-images-1309758663.cos.ap-nanjing.myqcloud.com/202307111631151.png" alt="image-20230711163103958" style="zoom:50%;float:left;" /> | <img src="https://blog-images-1309758663.cos.ap-nanjing.myqcloud.com/202307111631525.png" alt="image-20230711163159426" style="zoom:50%;float:left;" /> | <img src="https://blog-images-1309758663.cos.ap-nanjing.myqcloud.com/202307111633860.png" alt="image-20230711163332800" style="zoom:50%;" /> | <img src="https://blog-images-1309758663.cos.ap-nanjing.myqcloud.com/202307111634883.png" alt="image-20230711163417839" style="zoom:50%;" /> |

> 前端项目地址：https://github.com/humeng1010/bonfire-frontend





