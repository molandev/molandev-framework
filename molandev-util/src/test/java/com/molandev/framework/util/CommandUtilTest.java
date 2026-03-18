package com.molandev.framework.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandUtil 功能集成测试
 */
@DisplayName("命令工具类测试")
public class CommandUtilTest {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    @Nested
    @DisplayName("1. 基础执行测试")
    class BasicExecutionTest {

        @Test
        @DisplayName("验证跨平台基础命令执行 (java -version)")
        void testJavaVersion() {
            // java -version 输出通常在 stderr
            CommandUtil.CommandResult result = CommandUtil.execute("java", "-version");
            assertTrue(result.getExitCode() == 0 || result.getExitCode() == 1); 
            // 只要没报错抛异常，说明 ProcessBuilder 正常工作
            assertNotNull(result.getStderr());
        }

        @Test
        @DisplayName("验证 OS 平台特定命令")
        void testOsSpecificCommand() {
            CommandUtil.CommandResult result;
            if (IS_WINDOWS) {
                result = CommandUtil.execute("cmd.exe", "/c", "echo", "hello");
            } else {
                result = CommandUtil.execute("sh", "-c", "echo hello");
            }
            assertTrue(result.isSuccess());
            assertTrue(result.getStdout().contains("hello"));
        }
    }

    @Nested
    @DisplayName("2. 环境变量与工作目录测试")
    class EnvAndDirTest {

        @Test
        @DisplayName("验证环境变量传递")
        void testEnvironmentVariables() {
            Map<String, String> env = new HashMap<>();
            env.put("MOLAN_TEST_VAR", "molan_value");

            CommandUtil.CommandResult result;
            if (IS_WINDOWS) {
                result = CommandUtil.execute(env, "cmd.exe", "/c", "echo %MOLAN_TEST_VAR%");
            } else {
                result = CommandUtil.execute(env, "sh", "-c", "echo $MOLAN_TEST_VAR");
            }

            assertTrue(result.isSuccess());
            assertTrue(result.getStdout().contains("molan_value"));
        }

        @Test
        @DisplayName("验证工作目录切换")
        void testWorkDir() {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            CommandUtil.CommandResult result;
            if (IS_WINDOWS) {
                result = CommandUtil.execute(tempDir, "cmd.exe", "/c", "cd");
            } else {
                result = CommandUtil.execute(tempDir, "pwd");
            }

            assertTrue(result.isSuccess());
            // 验证输出中是否包含临时目录路径（忽略大小写和斜杠差异）
            String stdout = result.getStdout().toLowerCase().replace("\\", "/");
            String expected = tempDir.getAbsolutePath().toLowerCase().replace("\\", "/");
            assertTrue(stdout.contains(expected) || expected.contains(stdout));
        }
    }

    @Nested
    @DisplayName("3. 超时控制测试")
    class TimeoutTest {

        @Test
        @DisplayName("验证命令执行超时终止")
        void testTimeout() {
            CommandUtil.CommandResult result;
            if (IS_WINDOWS) {
                // Windows 下使用 ping 命令模拟 10 秒延迟 (ping -n 11 会等待 10 秒)
                result = CommandUtil.execute(2, TimeUnit.SECONDS, "ping", "-n", "11", "127.0.0.1");
            } else {
                result = CommandUtil.execute(2, TimeUnit.SECONDS, "sleep", "10");
            }

            assertFalse(result.isSuccess());
            assertTrue(result.isTimedOut());
            assertTrue(result.getStderr().contains("timed out"));
        }
    }

    @Nested
    @DisplayName("4. 异步执行测试")
    class AsyncTest {

        @Test
        @DisplayName("验证异步执行（火后即焚）")
        void testAsyncExecute() {
            // 异步执行由于不等待结果，主要验证不抛出异常
            assertDoesNotThrow(() -> {
                if (IS_WINDOWS) {
                    CommandUtil.executeAsync("cmd.exe", "/c", "echo async");
                } else {
                    CommandUtil.executeAsync("echo", "async");
                }
            });
        }
    }
}
