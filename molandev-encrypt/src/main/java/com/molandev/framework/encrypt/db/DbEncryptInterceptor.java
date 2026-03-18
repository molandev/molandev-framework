package com.molandev.framework.encrypt.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Properties;

/**
 * 参数拦截处理 仅支持对象内参数的加密，如果是简单的参数，建议直接使用util处理即可
 * 拦截 Executor 的 update 方法，包含 insert、update、delete 操作
 */
@Slf4j
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class DbEncryptInterceptor implements Interceptor {

    private static final String NAME_ENTITY = "et", NATIVE_SQL_ENTITY = "param1";

    /**
     * 加密解密
     */
    @Autowired
    private DbEncryptService dbEncryptService;

    @SuppressWarnings("rawtypes")
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取参数：args[0] 是 MappedStatement，args[1] 是参数对象
        Object[] args = invocation.getArgs();
        Object parameterObject = args[1];

        if (parameterObject != null) {
            if (parameterObject instanceof Map map) {
                // 处理 MyBatis-Flex 或 @Param 包装的情况
                // MyBatis-Flex 通常将实体放在 "et" key 中
                // 原生 MyBatis 的单参数通常放在 "param1" 中
                Object et = null;
                if (map.containsKey(NAME_ENTITY)) {
                    et = map.get(NAME_ENTITY);
                } else if (map.containsKey(NATIVE_SQL_ENTITY)) {
                    et = map.get(NATIVE_SQL_ENTITY);
                }
                
                if (et != null) {
                    // 对实体对象进行加密（只加密带 @Enc 注解的字段）
                    dbEncryptService.encrypt(et);
                }
            } else {
                // 直接是实体对象的情况
                dbEncryptService.encrypt(parameterObject);
            }
        }
        
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}
