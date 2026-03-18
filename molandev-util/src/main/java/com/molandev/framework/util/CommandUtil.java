package com.molandev.framework.util;

import lombok.Data;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 命令工具类
 * <p>
 * 用于在系统环境中执行命令行指令，支持 Linux 和 Windows。
 *
 * @author molandev
 */
public class CommandUtil {

    /**
     * 执行系统命令
     *
     * @param command 命令及参数，例如：execute("ls", "-l")
     * @return 执行结果封装对象
     */
    public static CommandResult execute(String... command) {
        return execute(null, null, 0, null, command);
    }

    /**
     * 执行系统命令，支持设置环境变量
     *
     * @param env     环境变量映射
     * @param command 命令及参数
     * @return 执行结果封装对象
     */
    public static CommandResult execute(Map<String, String> env, String... command) {
        return execute(null, env, 0, null, command);
    }

    /**
     * 执行系统命令，支持设置超时时间
     *
     * @param timeout 超时数值
     * @param unit    时间单位
     * @param command 命令及参数
     * @return 执行结果封装对象
     */
    public static CommandResult execute(long timeout, TimeUnit unit, String... command) {
        return execute(null, null, timeout, unit, command);
    }

    /**
     * 在指定工作目录下执行系统命令
     *
     * @param workDir 工作目录
     * @param command 命令及参数
     * @return 执行结果封装对象
     */
    public static CommandResult execute(File workDir, String... command) {
        return execute(workDir, null, 0, null, command);
    }

    /**
     * 在指定工作目录下执行系统命令，并支持设置环境变量和超时时间
     *
     * @param workDir 工作目录
     * @param env     环境变量映射
     * @param timeout 超时数值
     * @param unit    时间单位
     * @param command 命令及参数
     * @return 执行结果封装对象
     */
    public static CommandResult execute(File workDir, Map<String, String> env, long timeout, TimeUnit unit, String... command) {
        CommandResult result = new CommandResult();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (workDir != null && workDir.exists() && workDir.isDirectory()) {
                processBuilder.directory(workDir);
            }

            // 设置环境变量
            if (env != null && !env.isEmpty()) {
                processBuilder.environment().putAll(env);
            }

            Process process = processBuilder.start();

            // 等待执行完成并获取退出码
            if (timeout > 0 && unit != null) {
                boolean finished = process.waitFor(timeout, unit);
                if (!finished) {
                    process.destroyForcibly();
                    result.setTimedOut(true);
                    result.setSuccess(false);
                    result.setExitCode(-1);
                    result.setStderr("Command execution timed out after " + timeout + " " + unit);
                    return result;
                }
            } else {
                process.waitFor();
            }

            // 读取标准输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                result.setStdout(reader.lines().collect(Collectors.joining(System.lineSeparator())));
            }

            // 读取错误输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                result.setStderr(reader.lines().collect(Collectors.joining(System.lineSeparator())));
            }

            result.setExitCode(process.exitValue());
            result.setSuccess(result.getExitCode() == 0);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setExitCode(-1);
            result.setStderr(e.getMessage());
        }
        return result;
    }

    /**
     * 异步执行系统命令（火后即焚）
     * <p>
     * 该方法启动命令后立即返回，不等待执行完成，也不关注执行结果。
     * 内部自动将标准输出和错误输出重定向到系统丢弃设备，防止进程阻塞。
     *
     * @param command 命令及参数
     */
    public static void executeAsync(String... command) {
        executeAsync(null, null, command);
    }

    /**
     * 在指定目录下异步执行系统命令
     *
     * @param workDir 工作目录
     * @param env     环境变量
     * @param command 命令及参数
     */
    public static void executeAsync(File workDir, Map<String, String> env, String... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            if (workDir != null && workDir.exists() && workDir.isDirectory()) {
                processBuilder.directory(workDir);
            }
            if (env != null && !env.isEmpty()) {
                processBuilder.environment().putAll(env);
            }

            // 核心优化：将输出重定向到空设备，防止缓冲区满导致进程挂起
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD);

            processBuilder.start();
        } catch (Exception e) {
            // 异步启动失败，通常仅记录异常而不抛出
        }
    }

    /**
     * 命令执行结果封装类
     */
    @Data
    public static class CommandResult {
        /**
         * 是否执行成功（退出码为 0 则为成功）
         */
        private boolean success;

        /**
         * 是否执行超时
         */
        private boolean timedOut;

        /**
         * 退出码
         */
        private int exitCode;

        /**
         * 标准输出内容
         */
        private String stdout;

        /**
         * 错误输出内容
         */
        private String stderr;
    }
}
