package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("文件工具类测试")
class FileUtilsTest {

    @Nested
    @DisplayName("目录路径提取测试")
    class DirPathTest {

        @Test
        @DisplayName("getDir方法测试")
        void getDir() {
            // 测试包含目录的路径
            assertEquals("path/to", FileUtils.getDir("path/to/file.txt"));
            assertEquals("path", FileUtils.getDir("path/file.txt"));
            
            // 测试只有文件名的路径
            assertEquals("", FileUtils.getDir("file.txt"));
            
            // 测试以斜杠结尾的路径
            assertEquals("path/to/dir", FileUtils.getDir("path/to/dir/"));
            
            // 测试null值
            assertThrows(IllegalArgumentException.class, () -> FileUtils.getDir(null));
        }
    }

    @Nested
    @DisplayName("文件名提取测试")
    class FileNameTest {

        @Test
        @DisplayName("getFileName方法测试")
        void getFileName() {
            // 测试正常路径
            assertEquals("file.txt", FileUtils.getFileName("path/to/file.txt"));
            assertEquals("file.txt", FileUtils.getFileName("path/file.txt"));
            
            // 测试只有文件名的路径
            assertEquals("file.txt", FileUtils.getFileName("file.txt"));
            
            // 测试以斜杠结尾的路径
            assertEquals("path/to/dir/", FileUtils.getFileName("path/to/dir/"));
            
            // 测试null值
            assertThrows(NullPointerException.class, () -> FileUtils.getFileName(null));
        }
    }

    @Nested
    @DisplayName("文件扩展名提取测试")
    class FileExtTest {

        @Test
        @DisplayName("getFileExt方法测试")
        void getFileExt() {
            // 测试正常文件名
            assertEquals("txt", FileUtils.getFileExt("file.txt"));
            assertEquals("jpg", FileUtils.getFileExt("image.jpg"));
            
            // 测试多重扩展名
            assertEquals("tar.gz", FileUtils.getFileExt("archive.tar.gz"));
            assertEquals("tar.bz2", FileUtils.getFileExt("archive.tar.bz2"));
            assertEquals("tar.xz", FileUtils.getFileExt("archive.tar.xz"));
            assertEquals("tar.zst", FileUtils.getFileExt("archive.tar.zst"));
            
            // 测试没有扩展名的文件名
            assertEquals("", FileUtils.getFileExt("README"));
            assertEquals("", FileUtils.getFileExt(""));
            
            // 测试以点开头的文件名（隐藏文件）
            assertEquals("", FileUtils.getFileExt(".gitignore"));
            
            // 测试null值
            assertEquals("", FileUtils.getFileExt(null));
        }
    }

    @Nested
    @DisplayName("ContentType获取测试")
    class ContentTypeTest {

        @Test
        @DisplayName("getContentType根据文件名获取测试")
        void getContentTypeByFilename() {
            // 文本文件
            assertEquals("text/plain", FileUtils.getContentType("readme.txt"));
            assertEquals("text/html", FileUtils.getContentType("index.html"));
            assertEquals("text/css", FileUtils.getContentType("style.css"));
            assertEquals("application/javascript", FileUtils.getContentType("script.js"));
            assertEquals("application/json", FileUtils.getContentType("data.json"));
            assertEquals("application/xml", FileUtils.getContentType("config.xml"));
            assertEquals("text/csv", FileUtils.getContentType("data.csv"));
            
            // 图片文件
            assertEquals("image/jpeg", FileUtils.getContentType("photo.jpg"));
            assertEquals("image/jpeg", FileUtils.getContentType("image.jpeg"));
            assertEquals("image/png", FileUtils.getContentType("icon.png"));
            assertEquals("image/gif", FileUtils.getContentType("anim.gif"));
            assertEquals("image/bmp", FileUtils.getContentType("pic.bmp"));
            assertEquals("image/webp", FileUtils.getContentType("photo.webp"));
            assertEquals("image/svg+xml", FileUtils.getContentType("logo.svg"));
            assertEquals("image/x-icon", FileUtils.getContentType("favicon.ico"));
            
            // 视频文件
            assertEquals("video/mp4", FileUtils.getContentType("video.mp4"));
            assertEquals("video/x-msvideo", FileUtils.getContentType("movie.avi"));
            assertEquals("video/quicktime", FileUtils.getContentType("clip.mov"));
            assertEquals("video/x-ms-wmv", FileUtils.getContentType("video.wmv"));
            assertEquals("video/x-flv", FileUtils.getContentType("stream.flv"));
            assertEquals("video/webm", FileUtils.getContentType("video.webm"));
            
            // 音频文件
            assertEquals("audio/mpeg", FileUtils.getContentType("song.mp3"));
            assertEquals("audio/wav", FileUtils.getContentType("sound.wav"));
            assertEquals("audio/ogg", FileUtils.getContentType("music.ogg"));
            assertEquals("audio/mp4", FileUtils.getContentType("audio.m4a"));
            assertEquals("audio/flac", FileUtils.getContentType("hifi.flac"));
            
            // 文档文件
            assertEquals("application/pdf", FileUtils.getContentType("document.pdf"));
            assertEquals("application/msword", FileUtils.getContentType("old.doc"));
            assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", 
                        FileUtils.getContentType("new.docx"));
            assertEquals("application/vnd.ms-excel", FileUtils.getContentType("old.xls"));
            assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
                        FileUtils.getContentType("new.xlsx"));
            assertEquals("application/vnd.ms-powerpoint", FileUtils.getContentType("old.ppt"));
            assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation", 
                        FileUtils.getContentType("new.pptx"));
            
            // 压缩文件
            assertEquals("application/zip", FileUtils.getContentType("archive.zip"));
            assertEquals("application/x-rar-compressed", FileUtils.getContentType("package.rar"));
            assertEquals("application/x-7z-compressed", FileUtils.getContentType("files.7z"));
            assertEquals("application/x-tar", FileUtils.getContentType("backup.tar"));
            assertEquals("application/gzip", FileUtils.getContentType("compressed.gz"));
            
            // 未知类型
            assertEquals("application/octet-stream", FileUtils.getContentType("unknown.xyz"));
            assertEquals("application/octet-stream", FileUtils.getContentType("noext"));
            assertEquals("application/octet-stream", FileUtils.getContentType(""));
            assertEquals("application/octet-stream", FileUtils.getContentType((String)null));
        }

        @Test
        @DisplayName("getContentType根据Path对象获取测试")
        void getContentTypeByPathObject() {
            assertEquals("text/plain", FileUtils.getContentType(Paths.get("readme.txt")));
            assertEquals("image/png", FileUtils.getContentType(Paths.get("icon.png")));
            assertEquals("application/pdf", FileUtils.getContentType(Paths.get("document.pdf")));
            assertEquals("application/json", FileUtils.getContentType(Paths.get("/path/to/data.json")));
        }
        
        @Test
        @DisplayName("getContentType大小写不敏感测试")
        void getContentTypeCaseInsensitive() {
            assertEquals("image/png", FileUtils.getContentType("Image.PNG"));
            assertEquals("image/jpeg", FileUtils.getContentType("PHOTO.JPG"));
            assertEquals("application/pdf", FileUtils.getContentType("Document.PDF"));
        }
    }

    @Nested
    @DisplayName("MIME类型获取测试（已废弃）")
    @Deprecated
    class MimeTypeTest {

        @Test
        @DisplayName("getMimeType根据文件路径获取测试")
        void getMimeTypeByPath() {
            // 测试CSS文件
            assertEquals("text/css", FileUtils.getMimeType("style.css"));
            assertEquals("text/css", FileUtils.getMimeType("path/to/style.css"));
            
            // 测试JS文件
            assertEquals("text/javascript", FileUtils.getMimeType("script.js"));
            assertEquals("text/javascript", FileUtils.getMimeType("path/to/script.js"));
            
            // 测试RAR文件
            assertEquals("application/vnd.rar", FileUtils.getMimeType("archive.rar"));
            assertEquals("application/vnd.rar", FileUtils.getMimeType("path/to/archive.rar"));
            
            // 测试7Z文件
            assertEquals("application/x-7z-compressed", FileUtils.getMimeType("archive.7z"));
            assertEquals("application/x-7z-compressed", FileUtils.getMimeType("path/to/archive.7z"));
            
            // 测试null值
            assertThrows(IllegalArgumentException.class, () -> FileUtils.getMimeType((String)null));
        }

        @Test
        @DisplayName("getMimeType根据Path对象获取测试")
        void getMimeTypeByPathObject() {
            // 测试CSS文件
            assertEquals("text/css", FileUtils.getMimeType(Paths.get("style.css")));
            
            // 测试JS文件
            assertEquals("text/javascript", FileUtils.getMimeType(Paths.get("script.js")));
            
            // 测试TXT文件
            assertEquals("text/plain", FileUtils.getMimeType(Paths.get("readme.txt")));
        }
    }

    @Nested
    @DisplayName("文件大小格式化测试")
    class FileSizeFormatTest {

        @Test
        @DisplayName("getFileSize方法测试")
        void getFileSize() {
            // 测试字节级别
            assertEquals("100.0B", FileUtils.getFileSize(100));
            assertEquals("1023.0B", FileUtils.getFileSize(1023));
            
            // 测试KB级别
            assertEquals("1.0KB", FileUtils.getFileSize(1024));
            assertEquals("1.5KB", FileUtils.getFileSize(1536));
            assertEquals("1023.0KB", FileUtils.getFileSize(1047552));
            
            // 测试MB级别
            assertEquals("1.0MB", FileUtils.getFileSize(1048576));
            assertEquals("1.5MB", FileUtils.getFileSize(1572864));
            assertEquals("1023.0MB", FileUtils.getFileSize(1072693248));
            
            // 测试GB级别
            assertEquals("1.0GB", FileUtils.getFileSize(1073741824));
            assertEquals("1.5GB", FileUtils.getFileSize(1610612736));
            assertEquals("2.0GB", FileUtils.getFileSize(2147483648L));
        }
    }
}