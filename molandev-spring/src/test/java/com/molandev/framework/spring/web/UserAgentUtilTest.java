package com.molandev.framework.spring.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserAgentUtilTest {

    @Test
    public void testParseChromeOnWindows() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Chrome", info.getBrowser());
        assertTrue(info.getBrowserVersion().startsWith("98"));
        assertEquals("Windows 10", info.getOs());
        assertEquals("desktop", info.getDeviceType());
    }

    @Test
    public void testParseFirefoxOnMac() {
        String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:97.0) Gecko/20100101 Firefox/97.0";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Firefox", info.getBrowser());
        assertTrue(info.getBrowserVersion().startsWith("97"));
        assertEquals("macOS", info.getOs());
        assertEquals("desktop", info.getDeviceType());
    }

    @Test
    public void testParseSafariOnIphone() {
        String userAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Mobile/15E148 Safari/604.1";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Safari", info.getBrowser());
        assertEquals("iPhone iOS", info.getOs());
        assertEquals("mobile", info.getDeviceType());
    }

    @Test
    public void testParseAndroidChrome() {
        String userAgent = "Mozilla/5.0 (Linux; Android 12; SM-G975F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.101 Mobile Safari/537.36";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Chrome", info.getBrowser());
        assertEquals("Android", info.getOs());
        assertEquals("mobile", info.getDeviceType());
    }

    @Test
    public void testParseIpad() {
        String userAgent = "Mozilla/5.0 (iPad; CPU OS 15_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.3 Mobile/15E148 Safari/604.1";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Safari", info.getBrowser());
        assertEquals("iPad iOS", info.getOs());
        assertEquals("tablet", info.getDeviceType());
    }

    @Test
    public void testParseEdgeOnWindows() {
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36 Edg/98.0.4758.102";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Edge", info.getBrowser());
        assertTrue(info.getBrowserVersion().startsWith("98"));
        assertEquals("Windows 10", info.getOs());
        assertEquals("desktop", info.getDeviceType());
    }

    @Test
    public void testParseUnknownUserAgent() {
        String userAgent = "";
        UserAgentUtil.UserAgentInfo info = UserAgentUtil.parse(userAgent);
        
        assertEquals("Unknown", info.getBrowser());
        assertEquals("Unknown", info.getOs());
        assertEquals("desktop", info.getDeviceType());
    }

    @Test
    public void testIsMobileMethod() {
        String mobileUA = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.2 Mobile/15E148 Safari/604.1";
        String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        
        assertTrue(UserAgentUtil.isMobile(mobileUA));
        assertFalse(UserAgentUtil.isMobile(desktopUA));
    }

    @Test
    public void testIsTabletMethod() {
        String tabletUA = "Mozilla/5.0 (iPad; CPU OS 15_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.3 Mobile/15E148 Safari/604.1";
        String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        
        assertTrue(UserAgentUtil.isTablet(tabletUA));
        assertFalse(UserAgentUtil.isTablet(desktopUA));
    }

    @Test
    public void testIsDesktopMethod() {
        String tabletUA = "Mozilla/5.0 (iPad; CPU OS 15_3_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.3 Mobile/15E148 Safari/604.1";
        String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36";
        
        assertFalse(UserAgentUtil.isDesktop(tabletUA));
        assertTrue(UserAgentUtil.isDesktop(desktopUA));
    }
}