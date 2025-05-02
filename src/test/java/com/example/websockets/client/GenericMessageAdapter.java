package com.example.websockets.client;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.function.Consumer;

@AllArgsConstructor
public class GenericMessageAdapter<T> extends StompSessionHandlerAdapter {
    private final Class<T> messageType;
    private final Consumer<T> handler;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return messageType;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            handler.accept((T) payload);
        } catch (ClassCastException e) {
            throw new RuntimeException("Unknown keepalive message type " + payload.getClass().getName(), e);
        }
    }
}
