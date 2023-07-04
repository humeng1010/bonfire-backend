package com.usercenter.common;

import lombok.Data;

/**
 * 通用分页请求参数
 *
 * @author humeng
 */
@Data
public class PageRequest {

    private Long pageSize;

    private Long currentPage;

}
