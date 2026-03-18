package com.molandev.framework.lock.support.factory;

import com.molandev.framework.lock.support.model.Lock;
import com.molandev.framework.lock.support.model.LockInfo;

/**
 * 锁工厂接口
 */
public interface LockFactory {

    /**
     * 获取锁
     *
     * @param lockInfo 锁信息
     * @return 锁实例
     */
    Lock getLock(LockInfo lockInfo);

}
