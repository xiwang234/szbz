package xw.szbz.cn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import xw.szbz.cn.model.BusinessLog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 业务日志服务
 * 负责将业务数据以标准化 JSON 格式追加到日志文件
 * 日志目录：sybz/syslog/
 * 日志文件命名：business_YYYYMMDD.log
 */
@Service
public class BusinessLogService {

    private static final String LOG_DIR = "syslog";
    private static final String LOG_PREFIX = "business_";
    private static final String LOG_SUFFIX = ".log";
    
    private final ObjectMapper objectMapper;

    public BusinessLogService() {
        this.objectMapper = new ObjectMapper();
        // 注册 JavaTimeModule 支持 Java 8 时间类型
        this.objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期序列化为时间戳
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 启用缩进输出（每条日志一行，但格式化）
        this.objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
        
        // 初始化日志目录
        initLogDirectory();
    }

    /**
     * 初始化日志目录
     */
    private void initLogDirectory() {
        try {
            Path logPath = Paths.get(LOG_DIR);
            if (!Files.exists(logPath)) {
                Files.createDirectories(logPath);
                System.out.println("业务日志目录已创建: " + logPath.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("创建日志目录失败: " + e.getMessage());
        }
    }

    /**
     * 记录业务日志
     * @param log 业务日志对象
     */
    public void log(BusinessLog log) {
        try {
            // 生成日志ID（时间戳 + 3位随机数）
            if (log.getId() == null) {
                long timestamp = System.currentTimeMillis();
                int random = (int) (Math.random() * 1000);
                log.setId(timestamp * 1000 + random);
            }
            
            // 获取当天日志文件路径
            String logFileName = getLogFileName();
            Path logFilePath = Paths.get(LOG_DIR, logFileName);
            
            // 将日志对象转换为 JSON 字符串（单行）
            String jsonLog = objectMapper.writeValueAsString(log);
            
            // 追加写入日志文件（使用UTF-8编码）
            String logLine = jsonLog + System.lineSeparator();
            Files.write(logFilePath, 
                       logLine.getBytes(StandardCharsets.UTF_8),
                       StandardOpenOption.CREATE,
                       StandardOpenOption.APPEND);
            
        } catch (IOException e) {
            System.err.println("写入业务日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 获取当天日志文件名
     * 格式: business_YYYYMMDD.log
     */
    private String getLogFileName() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return LOG_PREFIX + date + LOG_SUFFIX;
    }

    /**
     * 获取日志目录的绝对路径
     */
    public String getLogDirectoryPath() {
        return Paths.get(LOG_DIR).toAbsolutePath().toString();
    }
}
