package com.molandev.framework.encrypt.hybrid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.encrypt.common.EncryptProperties;
import com.molandev.framework.util.StringUtils;
import com.molandev.framework.util.encrypt.AesUtil;
import com.molandev.framework.util.encrypt.RsaUtil;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 双层加密请求包装类
 * 用于解密请求体数据
 */
public class HybridEncryptRequestWrapper extends HttpServletRequestWrapper {

    private byte[] decryptedBody;
    private String aesKey;

    public HybridEncryptRequestWrapper(HttpServletRequest request,
                                       EncryptProperties encryptProperties,
                                       ObjectMapper objectMapper) throws IOException {
        super(request);

        // 读取请求体
        byte[] bodyBytes = StreamUtils.copyToByteArray(request.getInputStream());
        if (bodyBytes.length == 0) {
            this.decryptedBody = bodyBytes;
            return;
        }

        try {
            // 解析请求体JSON
            String requestBody = new String(bodyBytes, StandardCharsets.UTF_8);
            HybridEncryptedRequest encryptedRequest = objectMapper.readValue(
                    requestBody, HybridEncryptedRequest.class);

            // 1. 使用RSA私钥解密AES密钥
            this.aesKey = RsaUtil.privateDecrypt(encryptedRequest.getEncryptedKey(),
                    encryptProperties.getHybrid().getPrivateKey());

            // 2. 使用AES密钥解密数据
            String decryptedData = AesUtil.decrypt(encryptedRequest.getData(), this.aesKey,
                    encryptProperties.getHybrid().getAesAlgorithm());

            // 3. 将解密后的数据作为新的请求体
            this.decryptedBody = decryptedData.getBytes(StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new HybridEncryptException("请求解密失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decryptedBody);
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
            public void setReadListener(ReadListener readListener) {
                // Not implemented
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    public String getAesKey() {
        return aesKey;
    }

}
