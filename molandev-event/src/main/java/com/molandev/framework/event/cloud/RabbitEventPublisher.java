package com.molandev.framework.event.cloud;

import com.molandev.framework.event.core.EventHierarchyResolver;
import com.molandev.framework.event.core.MolanEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Set;

/**
 * RabbitMQ 模式事件发布实现
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitEventPublisher implements MolanEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;

    @Override
    public void publish(Object event) {
        if (event == null) return;

        Class<?> eventClass = event.getClass();
        Set<Class<?>> hierarchy = EventHierarchyResolver.getHierarchy(eventClass);

        // 为继承链上的每个类发布消息，实现冒泡监听
        for (Class<?> clazz : hierarchy) {
            String exchangeName = getExchangeName(clazz);
            
            // 确保交换机存在
            ensureExchange(exchangeName);
            
            // 发送消息，使用 Fanout 模式广播到绑定的所有 Queue
            rabbitTemplate.convertAndSend(exchangeName, "", event);
            log.debug("已发布事件到 RabbitMQ 交换机: {}", exchangeName);
        }
    }

    private String getExchangeName(Class<?> clazz) {
        return "molan.event." + clazz.getName();
    }

    private void ensureExchange(String name) {
        // 动态声明 Fanout 交换机
        amqpAdmin.declareExchange(new FanoutExchange(name, true, false));
    }
}
