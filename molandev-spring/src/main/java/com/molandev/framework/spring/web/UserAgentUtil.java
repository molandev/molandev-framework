package com.molandev.framework.spring.web;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User-Agent解析工具类
 * 用于解析User-Agent字符串，获取浏览器信息、操作系统信息等
 */
public class UserAgentUtil {

    // 浏览器正则表达式模式
    private static final Pattern BROWSER_PATTERNS[] = {
            Pattern.compile("(?i)Edg/([\\d.]+)"),
            Pattern.compile("(?i)Chrome/([\\d.]+)"),
            Pattern.compile("(?i)Firefox/([\\d.]+)"),
            Pattern.compile("(?i)Safari/([\\d.]+)"),
            Pattern.compile("(?i)MSIE\\s([\\d.]+)"),
            Pattern.compile("(?i)OPR/([\\d.]+)"),  // Opera
            Pattern.compile("(?i)Vivaldi/([\\d.]+)"),
            Pattern.compile("(?i)YaBrowser/([\\d.]+)"), // Yandex Browser
            Pattern.compile("(?i)UCBrowser/([\\d.]+)"),
            Pattern.compile("(?i)MicroMessenger/([\\d.]+)") // WeChat
    };

    private static final String BROWSER_NAMES[] = {
            "Edge",    "Chrome", "Firefox", "Safari", "Internet Explorer", "Opera", "Vivaldi", "Yandex", "UC Browser", "WeChat"
    };

    // 操作系统正则表达式模式
    private static final Pattern OS_PATTERNS[] = {
            Pattern.compile("(?i)Windows NT 10.0"),
            Pattern.compile("(?i)Windows NT 6.3"),
            Pattern.compile("(?i)Windows NT 6.2"),
            Pattern.compile("(?i)Windows NT 6.1"),
            Pattern.compile("(?i)Windows NT 6.0"),
            Pattern.compile("(?i)Windows NT 5.2"),
            Pattern.compile("(?i)Windows NT 5.1"),
            Pattern.compile("(?i)Windows NT 5.0"),
            Pattern.compile("(?i)Windows CE"),
            Pattern.compile("(?i)Android ([\\d.]+)"),
            Pattern.compile("(?i)iPad.*OS ([\\d_]+)"),
            Pattern.compile("(?i)iPhone.*OS ([\\d_]+)"),
            Pattern.compile("(?i)iPod"),
            Pattern.compile("(?i)Mac OS X ([\\d._]+)"),
            Pattern.compile("(?i)Mac OS X"),
            Pattern.compile("(?i)Linux"),
            Pattern.compile("(?i)Ubuntu"),
            Pattern.compile("(?i)FreeBSD"),
            Pattern.compile("(?i)SunOS"),
            Pattern.compile("(?i)NetBSD"),
            Pattern.compile("(?i)OpenBSD"),
            Pattern.compile("(?i)DragonFly"),
            Pattern.compile("(?i)CrOS"), // Chrome OS
            Pattern.compile("(?i)BlackBerry"),
            Pattern.compile("(?i)PlayBook"),
            Pattern.compile("(?i)BB10"),
            Pattern.compile("(?i)SymbianOS|SymbOS"),
            Pattern.compile("(?i)webOS|hpwOS"),
            Pattern.compile("(?i)MeeGo")
    };

    private static final String OS_NAMES[] = {
            "Windows 10", "Windows 8.1", "Windows 8", "Windows 7", "Windows Vista", "Windows Server 2003", "Windows XP",
            "Windows 2000", "Windows CE", "Android", "iPad iOS", "iPhone iOS", "iPod",
            "macOS", "macOS", "Linux", "Ubuntu", "FreeBSD", "SunOS", "NetBSD", "OpenBSD",
            "DragonFly", "Chrome OS", "BlackBerry", "PlayBook", "BB10", "SymbianOS", "webOS", "MeeGo"
    };

    // 设备类型检测
    private static final Pattern MOBILE_DEVICE_PATTERNS[] = {
            Pattern.compile("(?i)Mobile"),
            Pattern.compile("(?i)Android"),
            Pattern.compile("(?i)iPhone"),
            Pattern.compile("(?i)iPad"),
            Pattern.compile("(?i)iPod"),
            Pattern.compile("(?i)BlackBerry"),
            Pattern.compile("(?i)Windows Phone"),
            Pattern.compile("(?i)Opera Mini"),
            Pattern.compile("(?i)IEMobile")
    };

    private static final Pattern TABLET_DEVICE_PATTERNS[] = {
            Pattern.compile("(?i)Tablet"),
            Pattern.compile("(?i)iPad"),
            Pattern.compile("(?i)Android(?!.*Mobile)"),
            Pattern.compile("(?i)Touch"),
            Pattern.compile("(?i)Silk")
    };

    /**
     * 解析User-Agent字符串，返回完整的用户代理信息
     *
     * @param userAgent User-Agent字符串
     * @return UserAgentInfo对象，包含浏览器和操作系统信息
     */
    public static UserAgentInfo parse(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return new UserAgentInfo("Unknown", "Unknown", "Unknown", "desktop");
        }

        String browser = detectBrowser(userAgent);
        String browserVersion = detectBrowserVersion(userAgent, browser);
        String os = detectOperatingSystem(userAgent);
        String deviceType = detectDeviceType(userAgent);

        return new UserAgentInfo(browser, browserVersion, os, deviceType);
    }

    /**
     * 检测浏览器名称
     *
     * @param userAgent User-Agent字符串
     * @return 浏览器名称
     */
    public static String detectBrowser(String userAgent) {
        for (int i = 0; i < BROWSER_PATTERNS.length; i++) {
            if (BROWSER_PATTERNS[i].matcher(userAgent).find()) {
                return BROWSER_NAMES[i];
            }
        }
        return "Unknown";
    }

    /**
     * 检测浏览器版本
     *
     * @param userAgent User-Agent字符串
     * @param browser   浏览器名称
     * @return 浏览器版本号
     */
    public static String detectBrowserVersion(String userAgent, String browser) {
        if (browser.equals("Unknown")) {
            return "Unknown";
        }

        for (int i = 0; i < BROWSER_PATTERNS.length; i++) {
            if (BROWSER_NAMES[i].equals(browser)) {
                Matcher matcher = BROWSER_PATTERNS[i].matcher(userAgent);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return "Unknown";
    }

    /**
     * 检测操作系统
     *
     * @param userAgent User-Agent字符串
     * @return 操作系统名称
     */
    public static String detectOperatingSystem(String userAgent) {
        for (int i = 0; i < OS_PATTERNS.length; i++) {
            if (OS_PATTERNS[i].matcher(userAgent).find()) {
                return OS_NAMES[i];
            }
        }
        return "Unknown";
    }

    /**
     * 检测设备类型（mobile/tablet/desktop）
     *
     * @param userAgent User-Agent字符串
     * @return 设备类型：mobile, tablet, 或 desktop
     */
    public static String detectDeviceType(String userAgent) {
        // 检查是否是平板设备
        for (Pattern pattern : TABLET_DEVICE_PATTERNS) {
            if (pattern.matcher(userAgent).find()) {
                return "tablet";
            }
        }

        // 检查是否是移动设备
        for (Pattern pattern : MOBILE_DEVICE_PATTERNS) {
            if (pattern.matcher(userAgent).find()) {
                return "mobile";
            }
        }

        // 默认为桌面设备
        return "desktop";
    }

    /**
     * 判断是否为移动设备
     *
     * @param userAgent User-Agent字符串
     * @return 如果是移动设备返回true，否则返回false
     */
    public static boolean isMobile(String userAgent) {
        return "mobile".equals(detectDeviceType(userAgent));
    }

    /**
     * 判断是否为平板设备
     *
     * @param userAgent User-Agent字符串
     * @return 如果是平板设备返回true，否则返回false
     */
    public static boolean isTablet(String userAgent) {
        return "tablet".equals(detectDeviceType(userAgent));
    }

    /**
     * 判断是否为桌面设备
     *
     * @param userAgent User-Agent字符串
     * @return 如果是桌面设备返回true，否则返回false
     */
    public static boolean isDesktop(String userAgent) {
        return "desktop".equals(detectDeviceType(userAgent));
    }

    /**
     * UserAgent信息类，封装浏览器和操作系统信息
     */
    public static class UserAgentInfo {
        private final String browser;
        private final String browserVersion;
        private final String os;
        private final String deviceType;

        public UserAgentInfo(String browser, String browserVersion, String os, String deviceType) {
            this.browser = browser;
            this.browserVersion = browserVersion;
            this.os = os;
            this.deviceType = deviceType;
        }

        public String getBrowser() {
            return browser;
        }

        public String getBrowserVersion() {
            return browserVersion;
        }

        public String getOs() {
            return os;
        }

        public String getDeviceType() {
            return deviceType;
        }

        @Override
        public String toString() {
            return "UserAgentInfo{" +
                    "browser='" + browser + '\'' +
                    ", browserVersion='" + browserVersion + '\'' +
                    ", os='" + os + '\'' +
                    ", deviceType='" + deviceType + '\'' +
                    '}';
        }
    }
}