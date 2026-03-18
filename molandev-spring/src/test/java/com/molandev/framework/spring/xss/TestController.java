package com.molandev.framework.spring.xss;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/xss")
    public String xssTest(@RequestParam String input) {
        // 这个方法会经过XSS过滤
        return input;
    }

    @PostMapping("/xss-form")
    public String xssForm(@RequestParam String input) {
        // 这个方法会经过XSS过滤
        return input;
    }

    @GetMapping("/xss-ignore-method")
    @XssIgnore
    public String xssIgnoreMethodTest(@RequestParam String input) {
        // 这个方法因为有@XssIgnore注解，不会经过XSS过滤
        return input;
    }

    @PostMapping("/xss-ignore-method-post")
    @XssIgnore
    public String xssIgnoreMethodPostTest(@RequestParam String input) {
        // 这个POST方法因为有@XssIgnore注解，不会经过XSS过滤
        return input;
    }


    @PostMapping("/xss-json")
    public TestData xssJsonTest(@RequestBody TestData data) {
        // 这个方法用于测试JSON反序列化过程中的XSS过滤
        return data;
    }

    @PostMapping("/xss-requestbody-string")
    public String xssRequestBodyStringTest(@RequestBody String input) {
        System.out.println("fick" + input);
        // 这个方法用于测试@RequestBody String的XSS过滤
        return input;
    }

    @PostMapping("/xss-file")
    public String xssFileTest(@RequestParam("file") MultipartFile file,
                              @RequestParam(required = false) String description) {
        // 这个方法用于测试文件上传时的XSS过滤
        return description != null ? description : "file uploaded";
    }

    public static class TestData {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}