package com.example.websockets;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;

@AllArgsConstructor
public class StompSessionHandlerAdapterImpl extends StompSessionHandlerAdapter {
    private final int id;
    private final String type;

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return WebsocketResponse.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        if (payload instanceof WebsocketResponse response) {
            System.out.println(id + ": Received " + type + ": " + response);
        } else {
            System.err.println("Unknown " + type + " message type " + payload.getClass().getName());
        }
    }
}
