package com.zyz.swaggertoapidoc.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:30
 */
@Data
public class DocVO {
    /**
     * 大标题
     */
    private String title;
    /**
     * 小标题
     */
    private String tag;
    /**
     * url
     */
    private String url;

    /**
     * 描述
     */
    private String description;

    /**
     * 请求参数格式
     */
    private String requestForm;

    /**
     * 响应参数格式
     */
    private String responseForm;

    /**
     * 请求方式
     */
    private String requestType;

    private List<ReqVO> headerList;

    /**
     * 请求体
     */
    private List<ReqVO> requestList;



    /**
     * 返回属性列表
     */
    private PropertiesVO propertiesVo;
}
