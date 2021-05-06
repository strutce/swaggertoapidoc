package com.zyz.swaggertoapidoc.service;

import java.util.Map;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:27
 */
public interface DocService {
    Map<String, Object> getAttributes(String url);
}
