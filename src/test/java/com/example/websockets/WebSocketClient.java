package com.example.websockets;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketClient {
    private static final String URL = "ws://localhost:8080/ws";

    private final AtomicBoolean running = new AtomicBoolean(true);

    private StompSession session;

    public static void main(String[] args) {
        new WebSocketClient().run();
    }

    private void run() {
        connect();

        // Keep sending messages until user types 'exit'
        Scanner scanner = new Scanner(System.in);
        while (running.get()) {
            try {
                System.out.println("Enter message (or 'exit' to quit):");
                String message = scanner.nextLine();

                if ("exit".equalsIgnoreCase(message)) {
                    running.set(false);
                    if (session != null && session.isConnected()) {
                        session.disconnect();
                    }
                    System.exit(0);
                }

                if (session != null && session.isConnected()) {
                    session.send("/app/hello", message);
                } else {
                    System.out.println("Not connected. Attempting to reconnect...");
                    connect();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                System.out.println("Attempting to reconnect...");
                connect();
            }
        }
        scanner.close();
    }

    private void connect() {
        try {
            StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
            WebSocketTransport transport = new WebSocketTransport(webSocketClient);
            SockJsClient sockJsClient = new SockJsClient(List.of(transport));
            WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
            stompClient.setMessageConverter(new /*MappingJackson2MessageConverter*/StringMessageConverter());
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

    private class MyStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to WebSocket server");
            // Subscribe to the greeting topic
            session.subscribe("/topic/greetings", new StompSessionHandlerAdapter() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Received greeting: " + payload);
                }
            });
            // Subscribe to the timestamp topic
            session.subscribe("/topic/timestamp", new StompSessionHandlerAdapter() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    System.out.println("Received timestamp: " + payload);
                }
            });
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
