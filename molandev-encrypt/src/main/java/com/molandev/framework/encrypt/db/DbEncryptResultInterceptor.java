package com.molandev.framework.encrypt.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * 结果集拦截处理
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = Statement.class)})
@Slf4j
public class DbEncryptResultInterceptor implements Interceptor {

    @Autowired
    private DbEncryptService dbEncryptService;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (invocation.getTarget() instanceof ResultSetHandler) {
            Object result = invocation.proceed();
            if (Objects.isNull(result)) {
                return null;
            }
            if (result instanceof List<?> resultList) {
                if (!CollectionUtils.isEmpty(resultList)) {
                    dbEncryptService.decrypt(resultList);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }

}
