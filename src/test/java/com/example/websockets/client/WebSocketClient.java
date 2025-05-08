package com.example.websockets.client;

import com.example.websockets.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static com.example.websockets.Consts.*;

@RequiredArgsConstructor
public class WebSocketClient implements Closeable {
    @Getter
    private final int id;
    private final String url;
    private final boolean reconnectAutomatically;

    private StompSession session;

    @Override
    public void close() {
        disconnect();
    }

    public void handleCommand(String command) {
        if (!isConnected()) {
            System.out.println("Not connected. Attempting to reconnect...");
            connect();
        }
        send(new Messages.HelloRequest(command));
    }

    public void send(Messages.HelloRequest hello) {
        session.send(WEBSOCKETS_APP + REQUEST_GREETING, hello);
        session.send(WEBSOCKETS_APP + REQUEST_HELLO, hello);
    }

    public boolean isConnected() {
        return (session != null) && session.isConnected();
    }

    public void connect() {
        if (isConnected()) {
            return;
        }
        try {
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            session = stompClient.connectAsync(url, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to connect: " + e.getMessage());
        }
    }

    private static final WebSocketStompClient stompClient = getWebSocketStompClient();

    private static WebSocketStompClient getWebSocketStompClient() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketTransport transport = new WebSocketTransport(webSocketClient);
        SockJsClient sockJsClient = new SockJsClient(List.of(transport));
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(mappingJackson2MessageConverter)));
        return stompClient;
    }

    public void disconnect() {
        if (session != null) {
            try {
                session.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            session = null;
        }
    }

    private class MyStompSessionHandler implements StompSessionHandler {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server with session ID: " + session.getSessionId());
            subscribe(session, TOPIC_PREFIX + TOPIC_BROADCAST, new GenericMessageAdapter<>(Messages.KeepaliveBroadcast.class, b -> System.out.println("Keepalive " + b.time())));
            subscribe(session, TOPIC_PREFIX + TOPIC_JOIN, new GenericMessageAdapter<>(Messages.JoinBroadcast.class, response -> System.out.println(response.sessionId() + ": Received greeting: " + response.text())));
            subscribe(session, REPLY_PREFIX + QUEUE_PREFIX + RESPONSE_TO_HELLO, new GenericMessageAdapter<>(Messages.HelloResponse.class, response -> System.out.println(response.sessionId() + ": Received hello: " + response.text())));
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("Error: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.out.println("Transport error: " + exception.getMessage());
            if (reconnectAutomatically && !session.isConnected()) {
                try {
                    Thread.sleep(5000); // Wait before trying to reconnect
                    connect();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {

        }
    }

    protected static void subscribe(StompSession session, String destination, StompSessionHandlerAdapter adapter) {
        System.out.println("Subscribing to " + destination);
        session.subscribe(destination, adapter);
    }
}
