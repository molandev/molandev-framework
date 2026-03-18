package com.molandev.framework.spring.web;

import com.molandev.framework.spring.json.JSONUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.*;

/**
 * mvc工具
 */
public class MvcUtils {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };
    /**
     * 日志记录器
     */
    static Logger logger = LoggerFactory.getLogger("RequestErrorLog");

    /**
     * 得到request对象
     *
     * @return {@link HttpServletRequest}
     */
    public static HttpServletRequest getRequest() {
        try {
            return ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes()))
                    .getRequest();
        } catch (Exception e) {
            return null;
        }


    }

    /**
     * 获取响应
     *
     * @return {@link HttpServletResponse}
     */
    public static HttpServletResponse getResponse() {

        return ((ServletRequestAttributes) (RequestContextHolder
                .currentRequestAttributes())).getResponse();
    }

    /**
     * 得到header中的参数
     *
     * @param key 关键
     * @return {@link String}
     */
    public static String getHeader(String key) {

        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getHeader(key);
    }

    /**
     * 得到请求参数
     *
     * @param key 关键
     * @return {@link String}
     */
    public static String getRequestParam(String key) {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }
        return request.getParameter(key);
    }

    /**
     * 打印请求相关的日志
     */
    public static void logErrorRequest() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return;
        }
        String sb = "[REQ][" + request.getRequestURI() + "]" +
                JSONUtils.toJsonString(convertMap(request.getParameterMap()));
        logger.error(sb);
    }

    /**
     * 转换map
     *
     * @param paramMap 参数map
     */
    public static Map<String, String> convertMap(Map<String, String[]> paramMap) {
        Map<String, String> rtnMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            if (entry.getValue().length == 1) {
                rtnMap.put(entry.getKey(), entry.getValue()[0]);
            } else {
                rtnMap.put(entry.getKey(), Arrays.toString(entry.getValue()));
            }
        }
        return rtnMap;
    }

    /**
     * 获取客户端真实IP地址
     *
     * @param request HttpServletRequest
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = null;
        for (String header : IP_HEADERS) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }

        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP地址的情况，取第一个非unknown的有效IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }

        return ip;
    }

    /**
     * 获取客户端真实IP地址(使用当前请求)
     *
     * @return 客户端IP地址
     */
    public static String getClientIpAddress() {
        HttpServletRequest request = getRequest();
        return getClientIpAddress(request);
    }

    /**
     * 检查IP地址是否有效
     *
     * @param ip IP地址字符串
     * @return 是否为有效IP
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 获取User-Agent信息
     *
     * @param request HttpServletRequest
     * @return User-Agent字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    /**
     * 获取User-Agent信息(使用当前请求)
     *
     * @return User-Agent字符串
     */
    public static String getUserAgent() {
        HttpServletRequest request = getRequest();
        return getUserAgent(request);
    }


    /**
     * 获取所有请求参数名
     *
     * @param request HttpServletRequest
     * @return 参数名列表
     */
    public static List<String> getParameterNames(HttpServletRequest request) {
        if (request == null) {
            return java.util.Collections.emptyList();
        }

        List<String> paramNames = new java.util.ArrayList<>();
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            paramNames.add(names.nextElement());
        }
        return paramNames;
    }

    /**
     * 获取所有请求参数名(使用当前请求)
     *
     * @return 参数名列表
     */
    public static List<String> getParameterNames() {
        HttpServletRequest request = getRequest();
        return getParameterNames(request);
    }

    /**
     * 从请求中获取Referer头信息
     *
     * @param request HttpServletRequest
     * @return Referer信息
     */
    public static String getReferer(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("Referer");
    }

    /**
     * 从当前请求中获取Referer头信息
     *
     * @return Referer信息
     */
    public static String getReferer() {
        HttpServletRequest request = getRequest();
        return getReferer(request);
    }

    /**
     * 获取User-Agent解析信息
     *
     * @param request HttpServletRequest
     * @return UserAgentInfo对象，包含浏览器和操作系统信息
     */
    public static UserAgentUtil.UserAgentInfo getUserAgentInfo(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        return UserAgentUtil.parse(userAgent);
    }

    /**
     * 获取User-Agent解析信息(使用当前请求)
     *
     * @return UserAgentInfo对象，包含浏览器和操作系统信息
     */
    public static UserAgentUtil.UserAgentInfo getUserAgentInfo() {
        HttpServletRequest request = getRequest();
        return getUserAgentInfo(request);
    }

    /**
     * 判断当前请求是否来自移动设备
     *
     * @param request HttpServletRequest
     * @return 如果是移动设备返回true，否则返回false
     */
    public static boolean isMobileDevice(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        return UserAgentUtil.isMobile(userAgent);
    }

    /**
     * 判断当前请求是否来自移动设备(使用当前请求)
     *
     * @return 如果是移动设备返回true，否则返回false
     */
    public static boolean isMobileDevice() {
        HttpServletRequest request = getRequest();
        return isMobileDevice(request);
    }

    /**
     * 判断当前请求是否来自桌面设备
     *
     * @param request HttpServletRequest
     * @return 如果是桌面设备返回true，否则返回false
     */
    public static boolean isDesktopDevice(HttpServletRequest request) {
        String userAgent = getUserAgent(request);
        return UserAgentUtil.isDesktop(userAgent);
    }

    /**
     * 判断当前请求是否来自桌面设备(使用当前请求)
     *
     * @return 如果是桌面设备返回true，否则返回false
     */
    public static boolean isDesktopDevice() {
        HttpServletRequest request = getRequest();
        return isDesktopDevice(request);
    }
}
