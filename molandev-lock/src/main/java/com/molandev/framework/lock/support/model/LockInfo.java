package com.molandev.framework.lock.support.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * 锁信息
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LockInfo {

    /**
     * 锁的key值
     */
    private String key;

    /**
     * 获取锁的超时时间（秒）
     */
    private long waitTime;

    /**
     * 锁自动失效的时间（秒）
     */
    private long leaseTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LockInfo lockInfo = (LockInfo) o;
        return Objects.equals(key, lockInfo.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}
