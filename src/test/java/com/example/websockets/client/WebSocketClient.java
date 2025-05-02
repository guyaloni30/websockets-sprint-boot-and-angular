package com.example.websockets.client;

import com.example.websockets.Messages;
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

import static com.example.websockets.Consts.*;

@RequiredArgsConstructor
public class WebSocketClient {
    private static final String URL = "ws://localhost:8080/ws";
    private static final boolean reconnectAutomatically = false;

    private StompSession session;

    public void run() {
        // Keep sending messages until user types 'exit'
        try (Scanner scanner = new Scanner(System.in)) {
            connect();
            while (true) {
                try {
                    System.out.println("Enter command (or 'exit' to quit):");
                    String command = scanner.nextLine().trim();
                    if (!command.isEmpty()) {
                        handleCommand(command);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Attempting to reconnect...");
                    connect();
                }
            }
        }
    }

    private void handleCommand(String command) {
        if ("exit".equalsIgnoreCase(command)) {
            if (isConnected()) {
                session.disconnect();
            }
            System.exit(-1);
            return;
        }
        if ("disconnect".equalsIgnoreCase(command)) {
            if (session.isConnected()) {
                session.disconnect();
            }
            return;
        }
        if (!isConnected()) {
            System.out.println("Not connected. Attempting to reconnect...");
            connect();
        }
        Messages.HelloRequest hello = new Messages.HelloRequest(command);
        session.send(WEBSOCKETS_APP + REQUEST_GREETING, hello);
        session.send(WEBSOCKETS_APP + REQUEST_HELLO, hello);
    }

    private boolean isConnected() {
        return (session != null) && session.isConnected();
    }

    private void connect() {
        try {
            WebSocketStompClient stompClient = getWebSocketStompClient();
            StompSessionHandler sessionHandler = new MyStompSessionHandler();
            session = stompClient.connectAsync(URL, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Failed to connect: " + e.getMessage());
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying to reconnect
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static WebSocketStompClient getWebSocketStompClient() {
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketTransport transport = new WebSocketTransport(webSocketClient);
        SockJsClient sockJsClient = new SockJsClient(List.of(transport));
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(mappingJackson2MessageConverter)));
        return stompClient;
    }

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server with session ID: " + session.getSessionId());
            subscribe(session, TOPIC_PREFIX + TOPIC_BROADCAST, new GenericMessageAdapter<>(Messages.KeepaliveBroadcast.class, b -> System.out.println("Keepalive " + b.time())));
            subscribe(session, TOPIC_PREFIX + TOPIC_JOIN, new GenericMessageAdapter<>(Messages.JoinBroadcast.class, response -> System.out.println(response.sessionId() + ": Received greeting: " + response.text())));
            subscribe(session, REPLY_PREFIX + QUEUE_PREFIX + RESPONSE_TO_HELLO, new GenericMessageAdapter<>(Messages.HelloResponse.class, response -> System.out.println(response.sessionId() + ": Received hello: " + response.text())));
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
            if (reconnectAutomatically && !session.isConnected()) {
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
