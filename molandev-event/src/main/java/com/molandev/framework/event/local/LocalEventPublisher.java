package com.molandev.framework.event.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.molandev.framework.event.core.EventHierarchyResolver;
import com.molandev.framework.event.core.MolanEventPublisher;
import com.molandev.framework.event.core.MolanListenerDefinition;
import com.molandev.framework.event.core.MolanListenerProcessor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 本地模式事件发布实现
 */
@Slf4j
@RequiredArgsConstructor
public class LocalEventPublisher implements MolanEventPublisher {

    private final MolanListenerProcessor listenerProcessor;
    private final ThreadPoolTaskExecutor eventExecutor;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(Object event) {
        if (event == null) return;

        // 获取事件的继承链（冒泡）
        Set<Class<?>> hierarchy = EventHierarchyResolver.getHierarchy(event.getClass());
        
        // 找到匹配的监听器
        List<MolanListenerDefinition> allListeners = listenerProcessor.getListeners();
        
        for (Class<?> type : hierarchy) {
            List<MolanListenerDefinition> matched = allListeners.stream()
                    .filter(l -> l.getEventType().equals(type))
                    .collect(Collectors.toList());
            
            if (matched.isEmpty()) continue;

            // 按 Group 分组处理（争抢 vs 广播）
            Map<String, List<MolanListenerDefinition>> grouped = matched.stream()
                    .collect(Collectors.groupingBy(l -> l.getGroup() == null ? "" : l.getGroup()));

            grouped.forEach((group, list) -> {
                if (group.isEmpty()) {
                    // 广播模式：全部执行
                    list.forEach(l -> execute(l, event));
                } else {
                    // 争抢模式：同一 Group 只挑一个执行
                    MolanListenerDefinition selected = list.get(ThreadLocalRandom.current().nextInt(list.size()));
                    execute(selected, event);
                }
            });
        }
    }

    private void execute(MolanListenerDefinition definition, Object event) {
        // 深度克隆对象，防止监听器间的相互影响，并对齐微服务模式下的 JSON 转换行为
        Object clonedEvent = cloneEvent(event);
        
        if (definition.isAsync()) {
            eventExecutor.execute(() -> {
                try {
                    definition.invoke(clonedEvent);
                } catch (Exception e) {
                    log.error("异步监听器执行异常: " + definition.getMethod(), e);
                }
            });
        } else {
            try {
                definition.invoke(clonedEvent);
            } catch (Exception e) {
                log.error("同步监听器执行异常: " + definition.getMethod(), e);
                // 同步模式下重新抛出异常，保持原有的事务一致性
                throw e;
            }
        }
    }

    @SneakyThrows
    private Object cloneEvent(Object event) {
        if (event == null) return null;
        String json = objectMapper.writeValueAsString(event);
        return objectMapper.readValue(json, event.getClass());
    }
}
