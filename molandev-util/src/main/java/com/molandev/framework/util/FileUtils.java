package com.molandev.framework.util;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    /**
     * 根据路径获取dir
     */
    public static String getDir(String path) {
        if(StringUtils.isEmpty(path)){
            throw new IllegalArgumentException("路径不能为空");
        }
        if (path.contains("/")) {
            return path.substring(0, path.lastIndexOf("/"));
        }
        return "";
    }

    public static String getFileName(String path) {
        if (path.indexOf("/") > 1 && !path.endsWith("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }

    /**
     * 获取文件扩展名
     * @param fileName 文件名
     * @return 文件扩展名，如果没有扩展名则返回空字符串
     */
    public static String getFileExt(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        
        // 常见的多重扩展名组合
        String[] multiExts = {"tar.gz", "tar.bz2", "tar.xz", "tar.zst"};
        for (String multiExt : multiExts) {
            if (fileName.toLowerCase().endsWith("." + multiExt)) {
                return multiExt;
            }
        }
        
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 根据文件名推断 MIME 类型/ContentType
     * <p>支持 40+ 种常见文件格式的自动识别</p>
     * 
     * @param filename 文件名或文件路径
     * @return MIME 类型，未知类型返回 "application/octet-stream"
     */
    public static String getContentType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "application/octet-stream";
        }
        
        String lowerFilename = filename.toLowerCase();
        
        // 文本文件
        if (lowerFilename.endsWith(".txt")) return "text/plain";
        if (lowerFilename.endsWith(".html") || lowerFilename.endsWith(".htm")) return "text/html";
        if (lowerFilename.endsWith(".css")) return "text/css";
        if (lowerFilename.endsWith(".js")) return "application/javascript";
        if (lowerFilename.endsWith(".json")) return "application/json";
        if (lowerFilename.endsWith(".xml")) return "application/xml";
        if (lowerFilename.endsWith(".csv")) return "text/csv";
        
        // 图片文件
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) return "image/jpeg";
        if (lowerFilename.endsWith(".png")) return "image/png";
        if (lowerFilename.endsWith(".gif")) return "image/gif";
        if (lowerFilename.endsWith(".bmp")) return "image/bmp";
        if (lowerFilename.endsWith(".webp")) return "image/webp";
        if (lowerFilename.endsWith(".svg")) return "image/svg+xml";
        if (lowerFilename.endsWith(".ico")) return "image/x-icon";
        
        // 视频文件
        if (lowerFilename.endsWith(".mp4")) return "video/mp4";
        if (lowerFilename.endsWith(".avi")) return "video/x-msvideo";
        if (lowerFilename.endsWith(".mov")) return "video/quicktime";
        if (lowerFilename.endsWith(".wmv")) return "video/x-ms-wmv";
        if (lowerFilename.endsWith(".flv")) return "video/x-flv";
        if (lowerFilename.endsWith(".webm")) return "video/webm";
        
        // 音频文件
        if (lowerFilename.endsWith(".mp3")) return "audio/mpeg";
        if (lowerFilename.endsWith(".wav")) return "audio/wav";
        if (lowerFilename.endsWith(".ogg")) return "audio/ogg";
        if (lowerFilename.endsWith(".m4a")) return "audio/mp4";
        if (lowerFilename.endsWith(".flac")) return "audio/flac";
        
        // 文档文件
        if (lowerFilename.endsWith(".pdf")) return "application/pdf";
        if (lowerFilename.endsWith(".doc")) return "application/msword";
        if (lowerFilename.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (lowerFilename.endsWith(".xls")) return "application/vnd.ms-excel";
        if (lowerFilename.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (lowerFilename.endsWith(".ppt")) return "application/vnd.ms-powerpoint";
        if (lowerFilename.endsWith(".pptx")) return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
        
        // 压缩文件
        if (lowerFilename.endsWith(".zip")) return "application/zip";
        if (lowerFilename.endsWith(".rar")) return "application/x-rar-compressed";
        if (lowerFilename.endsWith(".7z")) return "application/x-7z-compressed";
        if (lowerFilename.endsWith(".tar")) return "application/x-tar";
        if (lowerFilename.endsWith(".gz")) return "application/gzip";
        
        // 默认类型
        return "application/octet-stream";
    }
    
    /**
     * 根据 Path 对象推断 MIME 类型/ContentType
     * 
     * @param file 文件路径对象
     * @return MIME 类型
     */
    public static String getContentType(Path file) {
        return getContentType(file.getFileName().toString());
    }

    /**
     * 根据文件路径判断 MIME 类型
     * 
     * @param filePath 文件路径
     * @return MIME 类型
     * @deprecated 使用 {@link #getContentType(String)} 替代，新方法支持更多文件类型且性能更好
     */
    @Deprecated
    public static String getMimeType(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("路径为空");
        }
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(filePath);
        if (null == contentType) {
            if (filePath.toUpperCase().endsWith(".CSS")) {
                contentType = "text/css";
            } else if (filePath.toUpperCase().endsWith(".JS")) {
                contentType = "text/javascript";
            } else if (filePath.toUpperCase().endsWith(".RAR")) {
                contentType = "application/vnd.rar";
            } else if (filePath.toUpperCase().endsWith(".7Z")) {
                contentType = "application/x-7z-compressed";
            }
        }

        if (null == contentType) {
            try {
                return Files.probeContentType(Paths.get(filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return contentType;
    }

    /**
     * 根据 Path 对象判断 MIME 类型
     * 
     * @param file 文件路径对象
     * @return MIME 类型
     * @deprecated 使用 {@link #getContentType(Path)} 替代
     */
    @Deprecated
    public static String getMimeType(Path file) {
        return getMimeType(file.toString());
    }

    /**
     * 将字节形式的大小转换为可读的数值
     */
    public static String getFileSize(long size) {
        double length = (double) size;
        //如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (length < 1024) {
            return length + "B";
        } else {
            length = length / 1024.0;
        }
        //如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        //因为还没有到达要使用另一个单位的时候
        //接下去以此类推
        if (length < 1024) {
            return Math.round(length * 100) / 100.0 + "KB";
        } else {
            length = length / 1024.0;
        }
        if (length < 1024) {
            //因为如果以MB为单位的话，要保留最后1位小数，
            //因此，把此数乘以100之后再取余
            return Math.round(length * 100) / 100.0 + "MB";
        } else {
            //否则如果要以GB为单位的，先除于1024再作同样的处理
            return Math.round(length / 1024 * 100) / 100.0 + "GB";
        }
    }

}
