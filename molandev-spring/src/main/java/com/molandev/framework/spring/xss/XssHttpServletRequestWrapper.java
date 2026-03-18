package com.molandev.framework.spring.xss;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * XSS HttpServletRequest 包装器
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    // 是否为multipart请求（文件上传）
    private final boolean multipartRequest;

    // 缓存已编码的请求体
    private String encodedBody;

    /**
     * 构造函数
     *
     * @param request 原始请求
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        this(request, false);
    }

    /**
     * 构造函数
     *
     * @param request          原始请求
     * @param multipartRequest 是否为multipart请求
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request, boolean multipartRequest) {
        super(request);
        this.multipartRequest = multipartRequest;
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (value == null) {
            return null;
        }
        String s = XssUtil.escape(value);
        return s;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = XssUtil.escape(values[i]);
        }
        return encodedValues;

    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalMap = super.getParameterMap();
        if (originalMap == null) {
            return null;
        }


        Map<String, String[]> encodedMap = new HashMap<>(originalMap.size());
        for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
            String[] values = entry.getValue();
            if (values != null) {
                int count = values.length;
                String[] encodedValues = new String[count];
                for (int i = 0; i < count; i++) {
                    encodedValues[i] = XssUtil.escape(values[i]);
                }
                encodedMap.put(entry.getKey(), encodedValues);
            } else {
                encodedMap.put(entry.getKey(), null);
            }
        }
        return encodedMap;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return super.getParameterNames();
    }

    @Override
    public String getHeader(String name) {
        // 特殊处理 Content-Length 头部
        if ("content-length".equalsIgnoreCase(name)) {
            if (!multipartRequest) {
                try {
                    // 返回编码后的内容长度
                    return String.valueOf(getEncodedBodyString().getBytes(StandardCharsets.UTF_8).length);
                } catch (IOException e) {
                    // 如果发生异常，返回原始长度
                    return super.getHeader(name);
                }
            }
        }

        // 不对header进行XSS过滤处理
        return super.getHeader(name);
    }


    @Override
    public Enumeration<String> getHeaders(String name) {
        // 特殊处理 Content-Length 头部
        if ("content-length".equalsIgnoreCase(name) && !multipartRequest) {
            try {
                // 返回编码后的内容长度
                List<String> headers = new ArrayList<>();
                headers.add(String.valueOf(getEncodedBodyString().getBytes(StandardCharsets.UTF_8).length));
                return Collections.enumeration(headers);
            } catch (IOException e) {
                // 如果发生异常，返回原始长度
                return super.getHeaders(name);
            }
        }

        // 不对headers进行XSS过滤处理
        return super.getHeaders(name);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        if (multipartRequest) {
            // 对于multipart请求，不处理请求体
            return super.getReader();
        }
        return new BufferedReader(new StringReader(getEncodedBodyString()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (multipartRequest) {
            // 对于multipart请求，不处理输入流
            return super.getInputStream();
        }

        // 处理普通请求的输入流
        String body = getEncodedBodyString();

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return new ServletInputStream() {

            private final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            @Override
            public int available() throws IOException {
                return byteArrayInputStream.available();
            }

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 不需要实现
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    private String getEncodedBodyString() throws IOException {
        if (encodedBody != null) {
            return encodedBody;
        }

        // 直接读取整个请求体，而不是逐行读取
        String body = StreamUtils.copyToString(super.getInputStream(), StandardCharsets.UTF_8);
        if (!body.isEmpty()) {
            // 判断是否为JSON格式的数据
            String contentType = super.getHeader("Content-Type");
            if (contentType != null && contentType.contains("application/json")) {
                // 对于JSON格式的数据，只过滤值部分，保持JSON结构完整
                body = XssUtil.escapeJson(body);
            } else {
                body = XssUtil.escape(body);
            }
        }

        encodedBody = body;
        return body;
    }
}