package com.example.websockets;

import lombok.RequiredArgsConstructor;
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
                    String message = scanner.nextLine();
                    handleMessage(message);
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
            session.send("/websockets-app/greeting", new MyWebsocketMessage(session.getSessionId(), id, message));
            session.send("/websockets-app/hello", new MyWebsocketMessage(session.getSessionId(), id, message));
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
            stompClient.setMessageConverter(new MyWebsocketMessageMessageConverter());
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
            subscribe(session, "/topic/greeting", "greeting");
            subscribe(session, "/topic/broadcast", "broadcast");
            subscribe(session, "/user/queue/hello", "hello");
        }

        private void subscribe(StompSession session, String destination, String type) {
            System.out.println("Subscribing to " + destination);
            session.subscribe(destination, new MessageHandler(id, type));
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
