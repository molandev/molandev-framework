package com.molandev.framework.lock.aspect;

import com.molandev.framework.lock.annotation.Idempotent;
import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.exception.IdempotentException;
import com.molandev.framework.lock.support.factory.LockFactory;
import com.molandev.framework.lock.support.model.LockInfo;
import com.molandev.framework.lock.utils.LockKeyUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 幂等切面
 */
@Aspect
public class IdempotentAspect {

    /**
     * key前缀
     */
    @Getter
    private static final String KEY_PREFIX = "Idempotent:";
    /**
     * 锁工厂
     */
    @Autowired
    private LockFactory lockFactory;

    @Pointcut("@annotation(com.molandev.framework.lock.annotation.Idempotent)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void beforePointCut(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (!method.isAnnotationPresent(Idempotent.class)) {
            return;
        }

        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        String key;

        // 若没有配置幂等标识编号，则使用 url + 参数列表作为区分
        if (!StringUtils.hasText(idempotent.key())) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                throw new IdempotentException("请求不存在！");
            }
            HttpServletRequest request = requestAttributes.getRequest();
            String url = request.getRequestURL().toString();
            String argString = Arrays.asList(joinPoint.getArgs()).toString();
            key = url + argString;
        } else {
            // 解析 EL 表达式
            key = LockKeyUtils.resolveKey(joinPoint, idempotent.key());
            if (key == null) {
                throw new IdempotentException("参数有误！");
            }
        }

        LockInfo lockInfo = new LockInfo(KEY_PREFIX + key, 0, idempotent.expireTime());
        Lock lock = lockFactory.getLock(lockInfo);
        boolean acquire = lock.acquire();

        if (!acquire) {
            // 获取不到锁，说明是重复点击，直接抛出异常
            String msg = idempotent.msg();
            throw new IdempotentException(msg);
        }
        // 为什么不释放？幂等是为了防止重复点击，即使业务已经执行完了，在短时间内依然不允许重复访问
    }

}
