package com.example.websockets.load;

import com.example.websockets.client.WebSocketClient;
import com.example.websockets.websockets.load.LoadRequest;
import com.example.websockets.websockets.load.LoadResponse;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.websockets.websockets.Consts.*;

public class LoadWebSocketClient extends WebSocketClient {
    private final AtomicInteger receivedMessages;
    private final int index;

    public LoadWebSocketClient(AtomicInteger receivedMessages, int index) {
        super(URI.create("ws://localhost:8080/ws"), WEBSOCKETS_APP);
        this.receivedMessages = receivedMessages;
        this.index = index;
    }

    public void send(long delay) {
        send(REQUEST_LOAD, new LoadRequest(delay, index * 2, index));
    }

    @Override
    protected void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        subscribe(session,
                TOPIC_PREFIX + RESPONSE_TO_LOAD,
                LoadResponse.class,
                _ -> receivedMessages.incrementAndGet());
    }
}
