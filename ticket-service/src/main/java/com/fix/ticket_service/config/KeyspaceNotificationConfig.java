package com.fix.ticket_service.config;

import com.fix.ticket_service.application.service.KeyExpiredListener;
import com.fix.ticket_service.application.service.TicketApplicationService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class KeyspaceNotificationConfig {

    private final RedisConnectionFactory factory;

    public KeyspaceNotificationConfig(RedisConnectionFactory factory) {
        this.factory = factory;
    }

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

    @PostConstruct
    public void enableKeyspaceEvents() {
        // Redis Keyspace Notifications 활성화
        factory.getConnection()
            .serverCommands()
            .setConfig("notify-keyspace-events", "Ex");
    }
}
