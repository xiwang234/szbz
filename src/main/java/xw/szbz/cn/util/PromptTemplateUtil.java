package xw.szbz.cn.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * 提示词模板工具类
 * 用于加载和渲染提示词模板
 */
@Component
public class PromptTemplateUtil {

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
     * 通用模板渲染方法
     * 
     * @param templatePath 模板文件路径（相对于resources目录）
     * @param variables 变量映射表
     * @return 渲染后的文本
     */
    public String renderTemplate(String templatePath, Map<String, String> variables) {
        try {
            // 读取模板文件
            ClassPathResource resource = new ClassPathResource(templatePath);
            String template = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
            
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
