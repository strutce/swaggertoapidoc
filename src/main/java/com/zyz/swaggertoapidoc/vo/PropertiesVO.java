package com.zyz.swaggertoapidoc.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:33
 */
@Data
public class PropertiesVO {
    @ApiModelProperty(value = "类名")
    private String className ;
    @ApiModelProperty(value = "属性名")
    private String name;
    @ApiModelProperty(value = "属性类型")
    private String type ;
    @ApiModelProperty(value = "是否必填")
    private Boolean require = false;
    @ApiModelProperty(value = "描述")
    private String description;
    @ApiModelProperty(value = "嵌套对象")
    private List<PropertiesVO> propertiesVOList =new ArrayList<>();
}
