package com.zyz.swaggertoapidoc.service.impl;

import com.alibaba.fastjson.JSON;
import com.zyz.swaggertoapidoc.service.DocService;
import com.zyz.swaggertoapidoc.vo.DocVO;
import com.zyz.swaggertoapidoc.vo.PropertiesVO;
import com.zyz.swaggertoapidoc.vo.ReqVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:28
 */
@Service
public class DocServiceImpl implements DocService {


    @Autowired
    RestTemplate restTemplate;

    @Override
    public Map<String, Object> getAttributes(String url) {
        //使用restTemplate 来请求swagger api-docs地址返回json字符串
        String jsonStr = restTemplate.getForObject(url, String.class);
        Map<String, Object> resultMap = new HashMap<>();
        List<DocVO> docVOList = new ArrayList<>();
        Map<String, Object> map = getAttributeMap(docVOList, jsonStr);
        // java8并行流来对控制类接口分组
        Map<String, List<DocVO>> controllerMap = docVOList.parallelStream().collect(Collectors.groupingBy(DocVO::getTitle));
        resultMap.put("controllerMap", controllerMap);
        resultMap.put("info", map.get("info"));
        return resultMap;
    }

    /**
     * @param docVOList
     * @param jsonStr
     * @Author zengyz
     * @Description
     * @Date 2021/4/16 14:07
     */
    private Map<String, Object> getAttributeMap(List<DocVO> docVOList, String jsonStr) {
        Map<String, Object> map = JSON.parseObject(jsonStr);
        Map<String, PropertiesVO> rspMap = getRspAttributeByDefinitins(map);
        Map<String, Map<String, Object>> paths = (Map<String, Map<String, Object>>) map.get("paths");
        if (paths != null) {
            Iterator<Map.Entry<String, Map<String, Object>>> pathIt = paths.entrySet().iterator();
            while (pathIt.hasNext()) {
                Map.Entry<String, Map<String, Object>> path = pathIt.next();
                Iterator<Map.Entry<String, Object>> requestIt = path.getValue().entrySet().iterator();
                String url = path.getKey();

                while (requestIt.hasNext()) {
                    Map.Entry<String, Object> request = requestIt.next();
                    // 请求方式
                    String requestType = request.getKey();
                    Map<String, Object> content = (Map<String, Object>) request.getValue();
                    // 控制类名称
                    String title = String.valueOf(((List) content.get("tags")).get(0));
                    // 接口名称
                    String tag = String.valueOf(content.get("summary"));
                    // 接口描述
                    String description = String.valueOf(content.get("summary"));
                    // 请求格式
                    String requestForm = "";
                    List<String> consumes = (List) content.get("consumes");
                    if (consumes != null && consumes.size() > 0) {
                        requestForm = StringUtils.join(consumes, ",");
                    }
                    // 返回格式
                    String responseForm = "";
                    List<String> produces = (List) content.get("produces");
                    if (produces != null && produces.size() > 0) {
                        responseForm = StringUtils.join(produces, ",");
                    }
                    // 请求对象
                    List<LinkedHashMap> parameters = (List<LinkedHashMap>) content.get("parameters");
                    // 返回对象
                    Map<String, Object> responses = (Map<String, Object>) content.get("responses");

                    DocVO docVO = new DocVO();
                    docVO.setTitle(title);
                    docVO.setUrl(url);
                    docVO.setTag(tag);
                    docVO.setDescription(description);
                    docVO.setRequestForm(requestForm);
                    docVO.setResponseForm(responseForm);
                    docVO.setRequestType(requestType);
                    //设置请求头
                    docVO.setHeaderList(dealReqParam(parameters, rspMap, "header"));
                    //设置请求对象，有时请求参数是body有时是query，参考该文章：https://www.jianshu.com/p/845411e73b86?from=timeline
                    docVO.setRequestList(dealReqParam(parameters, rspMap, "body,query"));

                    // 通过返回成功时引用的返回对象来获得前面封装的属性对象来做返回值使用
                    Map<String, Object> obj = (Map<String, Object>) responses.get("200");
                    PropertiesVO propertiesVo = null;
                    Map<String, Object> schema = null;
                    if (obj != null && obj.get("schema") != null) {
                        schema = (Map<String, Object>) obj.get("schema");
                        //其他类型
                        propertiesVo = new PropertiesVO();
                        propertiesVo.setType(StringUtils.defaultIfBlank((String) schema.get("type"), StringUtils.EMPTY));
                        if (schema.get("$ref") != null) {
                            propertiesVo = rspMap.get(schema.get("$ref"));
                        }
                        docVO.setPropertiesVo(propertiesVo);
                    }

                    docVOList.add(docVO);
                }
            }
        }
        return map;
    }

    /**
     * @param map
     * @Author zengyz
     * @Description 获得返回对象
     * @Date 2021/4/16 14:09
     */
    private Map<String, PropertiesVO> getRspAttributeByDefinitins(Map<String, Object> map) {
        // 通过definitions 来获得返回对象  "$ref": "#/definitions/返回对象" 如 "$ref": "#/definitions/BigScreenUserVO"
        Map<String, Map<String, Object>> definitionsMap = (Map<String, Map<String, Object>>) map.get("definitions");
        Map<String, PropertiesVO> propertiesVoMap = new HashMap<>();
        if (definitionsMap != null) {
            Iterator<String> rspIt = definitionsMap.keySet().iterator();
            while (rspIt.hasNext()) {
                String className = rspIt.next();
                dealRspParam(definitionsMap, propertiesVoMap, className);
            }
        }
        return propertiesVoMap;
    }

    /**
     * @param definitionsMap
     * @Author zengyz
     * @Description 将返回对象中的每个属性转成PropertiesVo对象
     * @Date 2021/4/16 14:29
     */
    private PropertiesVO dealRspParam(Map<String, Map<String, Object>> definitionsMap, Map<String, PropertiesVO> propertiesVoMap, String className) {
        String key = "#/definitions/" + className;
        PropertiesVO propertiesVo = propertiesVoMap.get(key);
        if (propertiesVo == null) {
            propertiesVo = new PropertiesVO();
            propertiesVoMap.put(key, propertiesVo);
        } else {
            return propertiesVo;
        }
        // 获得对象
        Map rspMap = definitionsMap.get(className);
        // 获得对象属性
        Map<String, Object> rspPropertiesMap = (Map<String, Object>) rspMap.get("properties");
        if (rspPropertiesMap == null) {
            return null;
        }
        Iterator<Map.Entry<String, Object>> rspPropertiesIt = rspPropertiesMap.entrySet().iterator();
        List<PropertiesVO> propertiesVOList = new ArrayList<>();
        Map.Entry<String, Object> propertiesMap = null;
        Map<String, Object> propertiesValueMap = null;
        String ref = null;
        Map items = null;
//        PropertiesVO childPropertiesVO = null;
        //将属性转成PropertiesVo对象
        while (rspPropertiesIt.hasNext()) {
            propertiesMap = rspPropertiesIt.next();
            propertiesValueMap = (Map<String, Object>) propertiesMap.getValue();
            PropertiesVO childPropertiesVo = new PropertiesVO();
            childPropertiesVo.setName(propertiesMap.getKey());
            childPropertiesVo.setType(StringUtils.defaultIfBlank((String) propertiesValueMap.get("type"), "Object"));
            ref = (String) propertiesValueMap.get("$ref");
            items = (Map) propertiesValueMap.get("items");
            if (ref != null || (items != null && (ref = (String) items.get("$ref")) != null)) {
                className = ref.replace("#/definitions/", "");
                PropertiesVO propertiesVo1 = dealRspParam(definitionsMap, propertiesVoMap, className);
                if (propertiesVo1 != null) {
                    childPropertiesVo.setPropertiesVOList(propertiesVo1.getPropertiesVOList());
                }
                if ("array".equals(childPropertiesVo.getType())) {
                    childPropertiesVo.setType("List<" + className + ">");
                }
            }
            childPropertiesVo.setDescription((String) propertiesValueMap.get("description"));
            propertiesVOList.add(childPropertiesVo);
        }
        Object title = rspMap.get("title");
        Object description = rspMap.get("description");
        propertiesVo.setClassName(title == null ? "" : title.toString());
        propertiesVo.setDescription(description == null ? "" : description.toString());
        propertiesVo.setPropertiesVOList(propertiesVOList);
        return propertiesVo;
    }


    /**
     * @param parameters
     * @Author zengyz
     * @Description 处理请求参数转成
     * @Date 2021/4/16 15:04
     */
    private List<ReqVO> dealReqParam(List<LinkedHashMap> parameters, Map<String, PropertiesVO> definitinMap, String type) {
        List<ReqVO> reqVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(parameters)) {
            String in;
            for (Map<String, Object> param : parameters) {
                in = (String) param.get("in");
                //请求参数中分请求头参数和请求体参数
                if (in != null) {
                    if (!type.contains(in)) {
                        continue;
                    }
                }
                ReqVO reqVO = new ReqVO();
                reqVO.setName(String.valueOf(param.get("name")));
                reqVO.setType(param.get("type") == null ? "Object" : param.get("type").toString());
                if (param.get("format") != null) {
                    reqVO.setType(reqVO.getType() + "<" + param.get("format") + ">");
                }
                // 是否必填
                reqVO.setRequire(false);
                if (param.get("required") != null) {
                    reqVO.setRequire((Boolean) param.get("required"));
                }
                // 参数说明
                reqVO.setRemark(String.valueOf(param.get("description")));
                reqVOList.add(reqVO);
            }
        }
        return reqVOList;
    }
}
