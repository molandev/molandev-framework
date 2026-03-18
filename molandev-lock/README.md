# MolanDev 分布式锁使用指南

## 概述

MolanDev Framework 提供了一个强大的分布式锁机制，支持通过注解和编程两种方式使用。本指南旨在帮助开发者正确使用分布式锁，避免常见的使用误区。

## 配置

可以通过 `application.properties` 或 `application.yml` 配置锁的类型：

```properties
# 锁类型: redis(基于RedisTemplate), redisson, memory
molan.lock.type=redis
```

- `redis`: 使用基于 RedisTemplate 的分布式锁实现
- `redisson`: 使用 Redisson 的分布式锁实现
- `memory`: 使用基于内存的锁实现（仅用于本地测试）

## 使用方式

### 方式一：编程式使用（推荐）

编程式使用提供了更高的灵活性和控制力，适合复杂业务场景。

#### 1. 基本用法
```java
@Service
public class OrderService {
    
    @Autowired
    private MolanLockHelper lockHelper;
    
    public void processOrder(String orderId) {
        // 使用订单ID作为锁的key，确保同一订单不会被重复处理
        lockHelper.runInLock("ORDER_PROCESS_" + orderId, () -> {
            // 业务逻辑
            System.out.println("Processing order: " + orderId);
            // 处理订单逻辑...
            return null;
        });
    }
    
    public String getOrderStatus(String orderId) {
        return lockHelper.runInLock("ORDER_STATUS_" + orderId, () -> {
            // 查询订单状态的业务逻辑
            return queryOrderStatus(orderId);
        });
    }
}
```

#### 2. 自定义超时时间
```java
@Service
public class PaymentService {
    
    @Autowired
    private MolanLockHelper lockHelper;
    
    public void processPayment(String paymentId) {
        // 自定义等待时间30秒，租约时间60秒
        lockHelper.runInLock("PAYMENT_" + paymentId, 30, 60, () -> {
            // 支付处理逻辑
            return processPaymentInternal(paymentId);
        });
    }
}
```

#### 3. 无返回值操作
```java
@Service
public class CacheService {
    
    @Autowired
    private MolanLockHelper lockHelper;
    
    public void clearCache(String cacheKey) {
        // 无返回值的操作
        lockHelper.runInLock("CACHE_CLEAR_" + cacheKey, () -> {
            // 清理缓存逻辑
            internalClearCache(cacheKey);
        });
    }
}
```

### 方式二：注解式使用

适用于简单的同步场景。

```java
@Service
public class InventoryService {
    
    @MolanLock(key = "'INVENTORY_' + #productId", waitTime = 10, leaseTime = 30)
    public boolean updateInventory(Long productId, Integer quantity) {
        // 库存更新逻辑
        return inventoryDao.update(productId, quantity);
    }
}
```

## 最佳实践

### 1. 锁的粒度控制
- **避免使用固定的锁key**：不要使用像"lockKey"这样的固定值
- **使用业务标识符**：使用实际的业务ID作为锁的key，如订单ID、用户ID等
- **合理设计key格式**：推荐使用 `"BUSINESS_TYPE_" + businessId` 的格式

### 2. 时间参数设置
- **waitTime（等待时间）**：获取锁的最大等待时间
  - 设置太短：可能导致频繁获取锁失败
  - 设置太长：可能导致资源长时间占用等待
  - 推荐值：10-30秒
- **leaseTime（租约时间）**：锁自动释放的时间
  - 必须大于业务执行时间
  - 防止因程序异常导致死锁
  - 推荐值：业务平均执行时间的2-3倍，但不超过60-120秒

### 3. 常见错误及解决方案

#### 错误示例1：固定锁key
```java
// ❌ 错误用法 - 所有操作使用同一把锁
@MolanLock(key = "'lockKey'")
public void processOrder(String orderId) {
    // 所有订单都会被同一把锁阻塞
}
```

```java
// ✅ 正确用法 - 每个订单使用独立的锁
@MolanLock(key = "'ORDER_' + #orderId")
public void processOrder(String orderId) {
    // 每个订单都有自己的锁，只有同一个订单不能同时处理
}
```

#### 错误示例2：时间参数设置不当
```java
// ❌ 错误用法 - 时间设置不合理
@MolanLock(waitTime = 1, leaseTime = 5) // 业务可能需要10秒执行完
public void longRunningTask() {
    // 长时间运行的任务，会导致锁提前释放
}
```

```java
// ✅ 正确用法 - 合理设置时间参数
@MolanLock(waitTime = 10, leaseTime = 30) // 考虑到业务执行时间
public void longRunningTask() {
    // 长时间运行的任务
}
```

### 4. 编程式 vs 注解式

| 特性 | 编程式 | 注解式 |
|------|--------|--------|
| 灵活性 | 高 | 低 |
| 控制力 | 强 | 弱 |
| 适用场景 | 复杂业务逻辑 | 简单同步场景 |
| 参数动态性 | 支持动态参数 | 主要支持SpEL表达式 |
| 推荐度 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |

## 注意事项

1. **避免嵌套锁**：尽量避免在同一业务流程中多次加锁
2. **异常处理**：锁会在finally块中自动释放，即使业务逻辑出现异常
3. **性能考虑**：高并发场景下，合理设计锁的粒度以平衡一致性与性能
4. **死锁预防**：leaseTime参数可防止因程序异常导致的死锁

## 示例场景

### 场景1：订单处理防重复提交
```java
@Service
public class OrderService {
    
    @Autowired
    private MolanLockHelper lockHelper;
    
    public OrderResult processOrder(OrderRequest request) {
        return lockHelper.runInLock("ORDER_PROCESS_" + request.getOrderId(), 15, 45, () -> {
            // 检查订单状态
            Order order = orderDao.getById(request.getOrderId());
            if (order.getStatus() != OrderStatus.CREATED) {
                throw new BusinessException("订单状态不允许处理");
            }
            
            // 更新订单
            order.setStatus(OrderStatus.PROCESSING);
            orderDao.update(order);
            
            // 处理业务逻辑
            return executeOrderProcess(request);
        });
    }
}
```

### 场景2：库存扣减
```java
@Service
public class InventoryService {
    
    public boolean deductInventory(Long productId, Integer quantity) {
        return lockHelper.runInLock("INVENTORY_DEDUCT_" + productId, 10, 30, () -> {
            // 查询当前库存
            Integer currentStock = inventoryDao.getCurrentStock(productId);
            if (currentStock < quantity) {
                throw new InsufficientStockException("库存不足");
            }
            
            // 扣减库存
            return inventoryDao.deduct(productId, quantity);
        });
    }
}
```