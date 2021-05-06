package com.zyz.swaggertoapidoc.vo;

import lombok.Data;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:33
 */
@Data
public class RspVO {
    /**
     * 返回参数
     */
    private String description;

    /**
     * 参数名
     */
    private String name;

    /**
     * 备注
     */
    private String remark;
}
