package com.example.websockets.client;

import com.example.websockets.Consts;
import com.example.websockets.messages.KeepaliveBroadcast;
import com.example.websockets.messages.MyWebsocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
public class WebSocketClient {
    private static final String URL = "ws://localhost:8080/ws";

    private final int id;

    private boolean running;
    private StompSession session;

    public void run() {
        // Keep sending messages until user types 'exit'
        try (Scanner scanner = new Scanner(System.in)) {
            connect();
            running = true;
            while (running) {
                try {
                    System.out.println("Enter message (or 'exit' to quit):");
                    String message = scanner.nextLine().trim();
                    if (!message.isEmpty()) {
                        handleMessage(message);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Attempting to reconnect...");
                    connect();
                }
            }
        }
    }

    private void handleMessage(String message) {
        if ("exit".equalsIgnoreCase(message)) {
            running = false;
            if (isConnected()) {
                session.disconnect();
            }
            System.exit(-1);
            return;
        }
        if (isConnected()) {
            session.send(Consts.WEBSOCKETS_APP + Consts.REQUEST_GREETING, new MyWebsocketMessage(session.getSessionId(), id, message));
            session.send(Consts.WEBSOCKETS_APP + Consts.REQUEST_HELLO, new MyWebsocketMessage(session.getSessionId(), id, message));
        } else {
            System.out.println("Not connected. Attempting to reconnect...");
            connect();
        }
    }

    private boolean isConnected() {
        return (session != null) && session.isConnected();
    }

    private void connect() {
        try {
            StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
            WebSocketTransport transport = new WebSocketTransport(webSocketClient);
            SockJsClient sockJsClient = new SockJsClient(List.of(transport));
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
            stompClient.setMessageConverter(new CompositeMessageConverter(List.of(mappingJackson2MessageConverter)));
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            session = stompClient.connectAsync(URL, sessionHandler).get();
            System.out.println("Session ID: " + session.getSessionId());
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Failed to connect: " + e.getMessage());
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying to reconnect
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server");
            subscribe(session, Consts.TOPIC_PREFIX + Consts.TOPIC_BROADCAST, new GenericMessageAdapter<>(KeepaliveBroadcast.class, b -> System.out.println("Keepalive " + b.time())));
            subscribe(session, Consts.TOPIC_PREFIX + Consts.TOPIC_JOIN, new GenericMessageAdapter<>(MyWebsocketMessage.class, response -> System.out.println(id + ": Received greeting: " + response)));
            subscribe(session, Consts.REPLY_PREFIX + Consts.QUEUE_PREFIX + Consts.RESPONSE_TO_HELLO, new GenericMessageAdapter<>(MyWebsocketMessage.class, response -> System.out.println(id + ": Received hello: " + response)));
        }

        private static void subscribe(StompSession session, String destination, StompSessionHandlerAdapter adapter) {
            System.out.println("Subscribing to " + destination);
            session.subscribe(destination, adapter);
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("Error: " + exception.getMessage());
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            System.out.println("Transport error: " + exception.getMessage());
            if (!session.isConnected()) {
                try {
                    Thread.sleep(5000); // Wait before trying to reconnect
                    connect();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
