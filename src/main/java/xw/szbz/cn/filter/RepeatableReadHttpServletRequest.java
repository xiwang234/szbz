package xw.szbz.cn.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 可重复读取的 HttpServletRequest 包装类
 * 用于在拦截器中读取 Body 后，Controller 中仍然可以读取
 */
public class RepeatableReadHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] body;

    public RepeatableReadHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 将 Body 读取并保存到内存中
        body = readBytes(request.getInputStream());
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // Not implemented
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    /**
     * 获取缓存的 Body 内容
     */
    public String getBody() {
        return new String(body, StandardCharsets.UTF_8);
    }

    /**
     * 读取 InputStream 到字节数组
     */
    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
