package com.molandev.framework.util;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * gzip工具
 */
public class GzipUtils {

    /**
     * 压缩
     *
     * @param bs 字节数组
     * @return {@link byte[]}
     */
    public static byte[] zip(byte[] bs) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (GZIPOutputStream outputStream = new GZIPOutputStream(os)) {
            outputStream.write(bs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    /**
     * 解压缩
     *
     * @param bs 字节数组
     * @return {@link byte[]}
     */
    public static byte[] unzip(byte[] bs) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ByteArrayInputStream is = null;
        GZIPInputStream gzipInputStream = null;

        try {
            is = new ByteArrayInputStream(bs);
            gzipInputStream = new GZIPInputStream(is);

            //
            byte[] buffer = new byte[1024];
            int c = -1;
            while ((c = gzipInputStream.read(buffer)) >= 0) {
                os.write(buffer, 0, c);
            }
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (gzipInputStream != null) {
                try {
                    gzipInputStream.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

}
