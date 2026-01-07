package xw.szbz.cn.exception;

/**
 * 业务异常类
 * 用于封装用户友好的错误消息，避免暴露系统技术细节
 */
public class ServiceException extends RuntimeException {

    /**
     * 用户友好的错误消息
     */
    private final String userMessage;

    /**
     * HTTP状态码
     */
    private final int statusCode;

    /**
     * 构造函数
     * @param userMessage 显示给用户的友好消息
     */
    public ServiceException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
        this.statusCode = 500;
    }

    /**
     * 构造函数（带状态码）
     * @param userMessage 显示给用户的友好消息
     * @param statusCode HTTP状态码
     */
    public ServiceException(String userMessage, int statusCode) {
        super(userMessage);
        this.userMessage = userMessage;
        this.statusCode = statusCode;
    }

    /**
     * 构造函数（带原始异常）
     * @param userMessage 显示给用户的友好消息
     * @param cause 原始异常（用于日志记录）
     */
    public ServiceException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.statusCode = 500;
    }

    /**
     * 构造函数（带状态码和原始异常）
     * @param userMessage 显示给用户的友好消息
     * @param statusCode HTTP状态码
     * @param cause 原始异常（用于日志记录）
     */
    public ServiceException(String userMessage, int statusCode, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
        this.statusCode = statusCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
