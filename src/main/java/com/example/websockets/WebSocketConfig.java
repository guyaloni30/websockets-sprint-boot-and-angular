package com.example.websockets;

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
        // Enable a simple in-memory message broker to send messages to clients
        // Messages with destination header starting with "/topic" will be routed to the broker
        config.enableSimpleBroker("/topic");
        // Messages with destination header starting with "/app" will be routed to @MessageMapping methods
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling SockJS fallback options so that
        // alternative messaging options may be used if WebSocket is not available
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // For development; restrict this in production
                .withSockJS();
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // Configure JSON as the default content type
        DefaultContentTypeResolver resolver = createResolver();
        // Create and configure Jackson converter
        MappingJackson2MessageConverter converter = createConverter(resolver);
        // Add to converters list
        messageConverters.add(converter);
        // Return true to indicate we've configured our own converters
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
