package com.molandev.framework.lock.support.model;

/**
 * 锁接口
 */
public interface Lock {

    /**
     * 获取锁
     *
     * @return 是否成功获取锁
     */
    boolean acquire();

    /**
     * 释放锁
     *
     * @return 是否成功释放锁
     */
    boolean release();

}
