package com.molandev.framework.spring.xss;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@XssIgnore
@RestController
@RequestMapping("/test")
public class XssIgnoredController {

    @GetMapping("/xss-ignore-class")
    public String xssIgnoreClassTest(@RequestParam String input) {
        // 这个类因为有@XssIgnore注解，不会经过XSS过滤
        return input;
    }

    @PostMapping("/xss-ignore-class-post")
    public String xssIgnoreClassPostTest(@RequestParam String input) {
        // 这个POST方法因为类上有@XssIgnore注解，不会经过XSS过滤
        return input;
    }

    @PostMapping("/xss-ignore-class-file")
    public String xssIgnoreClassFileTest(@RequestParam("file") MultipartFile file,
                                         @RequestParam String description) {
        // 这个文件上传方法因为类上有@XssIgnore注解，不会经过XSS过滤
        return description;
    }
}