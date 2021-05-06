package com.zyz.swaggertoapidoc.controller;

import com.zyz.swaggertoapidoc.service.DocService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @Author: zengyz
 * @Date: 2021/4/15 17:05
 */
@Api(tags = "swagger生成文档")
@Controller
public class DocController {

    @Autowired
    DocService docService;

    @Autowired
    SpringTemplateEngine springTemplateEngine;

    private String DEFAULT_FILENAME = "word";

    @ApiOperation(value = "预览界面")
    @GetMapping(value = "/preview")
    public String preview(Model model, String url) {
        getModel(model, url);
        return "doc";
    }

    private void getModel(Model model, String url) {
        Map<String, Object> attributesMap = docService.getAttributes(url);
        model.addAttribute("url", url);
        model.addAllAttributes(attributesMap);
    }

    /**
     * 下载文档
     *
     * @param model
     * @param url      需要转换成word文档的swagger地址
     * @param fileName 下载文件的文件名
     * @param response
     */
    @ApiOperation(value = "下载文档")
    @GetMapping(value = "/download")
    public void download(Model model, String url, String fileName, HttpServletResponse response) {
        getModel(model, url);
        downloadResponse(model, response, fileName);
    }

    private void downloadResponse(Model model, HttpServletResponse response, String fileName) {
        Context context = new Context();
        context.setVariables(model.asMap());
        String content = springTemplateEngine.process("doc", context);
        response.setContentType("application/octet-stream;charset=utf-8");
        response.setCharacterEncoding("utf-8");
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(response.getOutputStream());
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(StringUtils.isNotBlank(fileName) ? fileName : DEFAULT_FILENAME + ".doc", "utf-8"));
            byte[] bytes = content.getBytes();
            bos.write(bytes, 0, bytes.length);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
