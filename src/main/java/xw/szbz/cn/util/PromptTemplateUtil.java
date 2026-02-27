package xw.szbz.cn.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * 提示词模板工具类
 * 用于加载和渲染提示词模板
 */
@Component
public class PromptTemplateUtil {

    @Value("${prompt.base.path:/app/templates}")
    private String promptBasePath;

    /**
     * 加载六壬预测提示词模板并替换变量
     *
     * @param courseInfo 课传信息
     * @param question 占问事项
     * @param background 占问背景
     * @param birthInfo 出生信息（包含年份干支和性别）
     * @return 渲染后的提示词
     */
    public String renderLiuRenTemplate(String courseInfo, String question, String background, String birthInfo) {
        Map<String, String> variables = new HashMap<>();
        variables.put("courseInfo", courseInfo);
        variables.put("question", question);
        variables.put("background", background);
        variables.put("birthInfo", birthInfo);

        return renderTemplate("prompts/liuren_prediction_template.txt", variables);
    }

    /**
     * 加载六壬结果JSON提示词模板并替换变量（支持多语言）
     *
     * @param analysisText 分析文本
     * @param courseInfo 课传信息
     * @param question 占问事项
     * @param background 占问背景
     * @param language 语言标识（en/cn）
     * @return 渲染后的提示词
     */
    public String renderLiuRenResultJsonTemplate(String analysisText, String courseInfo, String question, String background, String language) {
        Map<String, String> variables = new HashMap<>();
        variables.put("analysisText", analysisText);
        variables.put("courseInfo", courseInfo);
        variables.put("question", question);
        variables.put("background", background);

        // 根据语言选择不同的模板文件
        String templatePath;
        if ("en".equalsIgnoreCase(language)) {
            templatePath = "prompts/liuren_result_json_template_en.txt";
        } else {
            // 默认使用中文模板（cn或其他值）
            templatePath = "prompts/liuren_result_json_template.txt";
        }

        return renderTemplate(templatePath, variables);
    }

    /**
     * 加载八字预测提示词模板并替换变量
     *
     * @param basicInfo 基本出生信息（四柱八字）
     * @param daYunStringList 大运及流年列表（到当前年份）
     * @param background 当前背景
     * @param daYunALLStringList 完整的10个大运及流年列表
     * @return 渲染后的提示词
     */
    public String renderBaZiTemplate(String basicInfo, String daYunStringList, String background, String daYunALLStringList) {
        Map<String, String> variables = new HashMap<>();
        variables.put("basicInfo", basicInfo);
        variables.put("daYunStringList", daYunStringList);
        variables.put("background", background != null ? background : "无特殊背景");
        variables.put("daYunALLStringList", daYunALLStringList);

        return renderTemplate("prompts/bazi_prediction_template.txt", variables);
    }

    /**
     * 通用模板渲染方法
     * 
     * @param templatePath 模板文件路径（相对于resources目录）
     * @param variables 变量映射表
     * @return 渲染后的文本
     */
    public String renderTemplate(String templatePath, Map<String, String> variables) {
        try {
            // 1. 优先从文件系统（挂载目录）读取
            String fullPath = Paths.get(promptBasePath, templatePath).toString();
            Resource fileResource = new FileSystemResource(fullPath);

            String template;
            if (fileResource.exists()) {
                // 从挂载目录读取
                template = Files.readString(fileResource.getFile().toPath(), StandardCharsets.UTF_8);
            } else {
                // 回退到类路径（兼容旧逻辑）
                ClassPathResource classPathResource = new ClassPathResource(templatePath);
                template = new String(classPathResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
            // 替换所有变量
            String result = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace(placeholder, value);
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException("无法加载模板文件: " + templatePath, e);
        }
    }

    /**
     * 从jar包中读取模板（用于生产环境）
     * 
     * @param templatePath 模板文件路径
     * @param variables 变量映射表
     * @return 渲染后的文本
     */
    public String renderTemplateFromJar(String templatePath, Map<String, String> variables) {
        try {
            // 从类路径读取资源（支持jar包）
            ClassPathResource resource = new ClassPathResource(templatePath);
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            
            // 替换所有变量
            String result = template;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue() : "";
                result = result.replace(placeholder, value);
            }
            
            return result;
        } catch (IOException e) {
            throw new RuntimeException("无法加载模板文件: " + templatePath, e);
        }
    }
}
