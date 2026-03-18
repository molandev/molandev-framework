package com.molandev.framework.encrypt.hybrid;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 双层加密响应包装类
 * 用于缓存响应数据，以便后续加密
 */
public class HybridEncryptResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final ServletOutputStream servletOutputStream;
    private final PrintWriter writer;

    public HybridEncryptResponseWrapper(HttpServletResponse response) {
        super(response);

        this.servletOutputStream = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // Not implemented
            }

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }
        };

        this.writer = new PrintWriter(outputStream);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    /**
     * 获取响应数据
     */
    public byte[] getResponseData() {
        writer.flush();
        return outputStream.toByteArray();
    }

}
