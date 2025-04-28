package com.fix.ticket_service.config;

import com.fix.ticket_service.application.service.KeyExpiredListener;
import com.fix.ticket_service.application.service.TicketApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class KeyspaceNotificationConfig {
    @Bean
    public RedisMessageListenerContainer redisListenerContainer(
        RedisConnectionFactory factory,
        KeyExpiredListener keyExpiredListener ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(keyExpiredListener, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }

    @Bean
    public KeyExpiredListener keyExpiredListener(TicketApplicationService service) {
        return new KeyExpiredListener(service);
    }
}
