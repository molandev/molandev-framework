package com.molandev.framework.event.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.molandev.framework.event.cloud.RabbitEventPublisher;
import com.molandev.framework.event.cloud.RabbitListenerRegistrar;
import com.molandev.framework.event.core.EventUtil;
import com.molandev.framework.event.core.MolanEventPublisher;
import com.molandev.framework.event.core.MolanListenerProcessor;
import com.molandev.framework.event.local.LocalEventPublisher;
import com.molandev.framework.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadFactory;

/**
 * 事件模块自动配置
 */
@Slf4j
@AutoConfiguration
public class EventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public static MolanListenerProcessor molanListenerProcessor() {
        return new MolanListenerProcessor();
    }

    /**
     * 创建内部使用的 ObjectMapper，支持类型保留
     */
    private static ObjectMapper createInternalObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 关键：激活默认类型保留，支持 List、泛型等
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }



    @Bean
    public InitializingBean eventUtilInitializer(MolanEventPublisher publisher) {
        return () -> {
            EventUtil.setPublisher(publisher);
            log.info("MolanEvent 系统初始化完成，发布器类型为: {}", publisher.getClass().getSimpleName());
        };
    }

    /**
     * 本地模式配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "molandev", name = "run-mode", havingValue = "single")
    static class LocalEventConfig {


        @Bean
        @ConditionalOnMissingBean
        public MolanEventPublisher localEventPublisher(MolanListenerProcessor processor, 
                                                        ThreadPoolTaskExecutor eventExecutor) {
            // 使用内部 ObjectMapper，不注册为 Bean
            return new LocalEventPublisher(processor, eventExecutor, createInternalObjectMapper());
        }

        @Bean(name = "eventExecutor")
        @ConditionalOnMissingBean
        public ThreadPoolTaskExecutor eventExecutor() {
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            
            // 检查是否支持虚拟线程
            if (ThreadUtil.isVirtualThreadsSupported()) {
                // 使用虚拟线程
                executor.setThreadFactory(ThreadUtil.createVirtualThreadFactory());
                log.info("MolanEvent 线程池使用虚拟线程");
            } else {
                // 使用普通线程
                executor.setCorePoolSize(5);
                executor.setMaxPoolSize(20);
                executor.setQueueCapacity(100);
                executor.setThreadNamePrefix("molan-event-");
                log.info("MolanEvent 线程池使用普通线程");
            }
            
            executor.initialize();
            return executor;
        }

    }

    /**
     * 云端模式配置 (RabbitMQ)
     */
    @Configuration
    @ConditionalOnProperty(prefix = "molandev", name = "run-mode", havingValue = "cloud")
    @ConditionalOnClass({RabbitTemplate.class, AmqpAdmin.class})
    static class CloudEventConfig {

        /**
         * 创建内部使用的 MessageConverter
         */
        private static MessageConverter createInternalMessageConverter(ObjectMapper objectMapper) {
            Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
            DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
            typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);
            typeMapper.addTrustedPackages("*"); // 信任所有包，配合 TypeId 实现精准还原
            converter.setJavaTypeMapper(typeMapper);
            return converter;
        }


        @Bean
        @ConditionalOnMissingBean
        public MolanEventPublisher rabbitEventPublisher(RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin) {
            // 为 RabbitTemplate 设置内部转换器
            ObjectMapper mapper = createInternalObjectMapper();
            rabbitTemplate.setMessageConverter(createInternalMessageConverter(mapper));
            return new RabbitEventPublisher(rabbitTemplate, amqpAdmin);
        }

        @Bean
        @ConditionalOnMissingBean
        public RabbitListenerRegistrar rabbitListenerRegistrar(MolanListenerProcessor processor,
                                                                ConnectionFactory connectionFactory,
                                                                AmqpAdmin amqpAdmin) {
            ObjectMapper mapper = createInternalObjectMapper();
            MessageConverter converter = createInternalMessageConverter(mapper);

            return new RabbitListenerRegistrar(processor, connectionFactory, amqpAdmin, converter);
        }
    }
}
