package com.molandev.framework.lock.aspect;

import com.molandev.framework.lock.annotation.GlobalLock;
import com.molandev.framework.lock.exception.LockTimeoutException;
import com.molandev.framework.lock.support.model.LockInfo;
import com.molandev.framework.lock.utils.LockInfoUtils;
import com.molandev.framework.lock.utils.LockUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 锁切面
 */
@Aspect
@Order(0)
@Slf4j
public class LockAspect {

    /**
     * 加锁的 AOP
     *
     * @param joinPoint 连接点
     * @param globalLock     锁注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around(value = "@annotation(globalLock)")
    public Object around(ProceedingJoinPoint joinPoint, GlobalLock globalLock) throws Throwable {
        LockInfo lockInfo = LockInfoUtils.getLockInfo(joinPoint, globalLock);

        try {
            return LockUtils.runInLock(lockInfo, () -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (LockTimeoutException e) {
            // 如果自定义了获取锁失败的处理策略，则执行自定义的降级处理策略
            if (StringUtils.hasText(globalLock.timeoutFallback())) {
                return handleCustomLockTimeout(globalLock.timeoutFallback(), joinPoint);
            } else {
                throw e;
            }
        }
    }

    /**
     * 处理自定义加锁超时
     *
     * @param lockTimeoutHandler 超时处理方法名
     * @param joinPoint          连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    private Object handleCustomLockTimeout(String lockTimeoutHandler, JoinPoint joinPoint) throws Throwable {
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod;

        try {
            handleMethod = target.getClass().getDeclaredMethod(
                    lockTimeoutHandler,
                    currentMethod.getParameterTypes()
            );
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal timeout fallback method: " + lockTimeoutHandler, e);
        }

        Object[] args = joinPoint.getArgs();

        try {
            return handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access timeout fallback method", e);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

}
