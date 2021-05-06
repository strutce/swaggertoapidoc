package com.zyz.swaggertoapidoc.vo;

import lombok.Data;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:32
 */
@Data
public class ReqVO {
    /**
     * 参数名
     */
    private String name;

    /**
     * 数据类型
     */
    private String type;

    /**
     * 是否必填
     */
    private Boolean require;

    /**
     * 说明
     */
    private String remark;

    /**
     * 复杂对象引用
     */
    private PropertiesVO propertiesVo;
}
