package com.molandev.framework.event.cloud;

import com.molandev.framework.event.core.MolanListenerDefinition;
import com.molandev.framework.event.core.MolanListenerProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.InitializingBean;

import java.util.UUID;

/**
 * RabbitMQ 监听器注册器
 */
@Slf4j
@RequiredArgsConstructor
public class RabbitListenerRegistrar implements InitializingBean {

    private final MolanListenerProcessor listenerProcessor;
    private final ConnectionFactory connectionFactory;
    private final AmqpAdmin amqpAdmin;
    private final MessageConverter messageConverter;

    @Override
    public void afterPropertiesSet() {
        for (MolanListenerDefinition definition : listenerProcessor.getListeners()) {
            registerListener(definition);
        }
    }

    private void registerListener(MolanListenerDefinition definition) {
        String exchangeName = "molan.event." + definition.getEventType().getName();
        String group = definition.getGroup();
        
        // 1. 声明交换机
        amqpAdmin.declareExchange(new FanoutExchange(exchangeName, true, false));

        // 2. 声明队列
        Queue queue;
        if (group == null || group.isEmpty()) {
            // 广播模式：随机队列名，非持久，独占，自动删除
            String anonymousQueueName = "molan.event.gen." + UUID.randomUUID();
            queue = new Queue(anonymousQueueName, false, true, true);
        } else {
            // 争抢模式：固定队列名，持久
            queue = new Queue("molan.event.group." + group, true, false, false);
        }
        amqpAdmin.declareQueue(queue);

        // 3. 绑定队列到交换机
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(new FanoutExchange(exchangeName)));

        // 4. 创建并启动容器
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queue.getName());
        
        // 设置消息处理器
        MessageListenerAdapter adapter = new MessageListenerAdapter(definition.getBean(), messageConverter);
        adapter.setDefaultListenerMethod(definition.getMethod().getName());
        container.setMessageListener(adapter);
        
        container.setAfterReceivePostProcessors(message -> {
            // 这里可以处理异步逻辑，但 SimpleMessageListenerContainer 本身就是异步的（在消费者线程中执行）
            return message;
        });

        container.start();
        log.info("已注册 RabbitMQ 监听器，事件: {}, 队列: {}, 分组: {}", 
                definition.getEventType().getSimpleName(), queue.getName(), group);
    }
}
