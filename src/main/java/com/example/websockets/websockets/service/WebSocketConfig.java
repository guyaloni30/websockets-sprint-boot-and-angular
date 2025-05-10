package com.example.websockets.websockets.service;

import com.example.websockets.websockets.Consts;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@AllArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes(Consts.WEBSOCKETS_APP);
        config.enableSimpleBroker(Consts.TOPIC_PREFIX, Consts.QUEUE_PREFIX);
        config.setUserDestinationPrefix(Consts.REPLY_PREFIX);//That's the default
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options so that alternative messaging options may be used if WebSocket is not available
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // For development; restrict this in production
                .withSockJS();
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        DefaultContentTypeResolver resolver = createResolver();
        MappingJackson2MessageConverter converter = createConverter(resolver);
        messageConverters.add(converter);
        return true;
    }

    private static DefaultContentTypeResolver createResolver() {
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        return resolver;
    }

    private static MappingJackson2MessageConverter createConverter(DefaultContentTypeResolver resolver) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setContentTypeResolver(resolver);
        return converter;
    }
}
