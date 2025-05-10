package com.example.websockets.mains;

import com.example.websockets.client.WebSocketClient;
import com.example.websockets.websockets.Messages;
import lombok.Getter;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;

import java.net.URI;
import java.util.Scanner;
import java.util.function.Consumer;

import static com.example.websockets.websockets.Consts.*;

public class MainWebSocketClient extends WebSocketClient {
    @Getter
    private final int id;
    private final boolean reconnectAutomatically;

    private boolean running;

    public MainWebSocketClient(int id) {
        super(URI.create("ws://localhost:8080/ws"), WEBSOCKETS_APP);
        this.id = id;
        this.reconnectAutomatically = false;
    }

    @Override
    protected final <T> void subscribe(StompSession session, String destination, Class<T> messageType, Consumer<T> handler) {
        System.out.println("Subscribing to " + destination);
        super.subscribe(session, destination, messageType, handler);
    }

    @Override
    protected void onConnect() {
        super.onConnect();
        System.out.println("Connected");
    }

    @Override
    protected void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("Connected to WebSocket server with session ID: " + session.getSessionId());
        subscribe(
                session,
                TOPIC_PREFIX + TOPIC_BROADCAST,
                Messages.KeepaliveBroadcast.class,
                broadcast -> System.out.println("Keepalive " + broadcast.time()));
        subscribe(
                session,
                TOPIC_PREFIX + TOPIC_JOIN,
                Messages.JoinBroadcast.class,
                response -> System.out.println(response.sessionId() + ": Received greeting: " + response.text()));
        subscribe(
                session,
                REPLY_PREFIX + QUEUE_PREFIX + RESPONSE_TO_HELLO,
                Messages.HelloResponse.class,
                response -> System.out.println(response.sessionId() + ": Received hello: " + response.text()));
    }

    @Override
    protected void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        System.out.println("Error: " + exception.getClass().getName() + ": " + exception.getMessage() + ", command: " + command + ", headers: " + headers + ((payload != null) ? ", payload: " + new String(payload) : ""));
    }

    @Override
    protected void handleTransportError(StompSession session, Throwable exception) {
        System.out.println("Transport error: " + exception.getClass().getName() + ": " + exception.getMessage());
        if (reconnectAutomatically && !isConnected()) {
            try {
                Thread.sleep(5000); // Wait before trying to reconnect
                connect();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void run() {
        // Keep sending messages until user types 'exit'
        safeConnect();
        try (Scanner scanner = new Scanner(System.in)) {
            running = true;
            while (running) {
                System.out.println("Enter command (or 'exit' to quit):");
                String command = scanner.nextLine().trim();
                handleCommand(command);
            }
        }
    }

    public void handleCommand(String command) {
        if (command.isEmpty()) {
            return;
        }
        try {
            if ("exit".equalsIgnoreCase(command)) {
                disconnect();
                running = false;
                return;
            }
            if ("disconnect".equalsIgnoreCase(command)) {
                disconnect();
                return;
            }
            if (!isConnected()) {
                System.out.println("Not connected. Attempting to reconnect...");
                connect();
            }
            Messages.HelloRequest hello = new Messages.HelloRequest(command);
            send(REQUEST_GREETING, hello);
            send(REQUEST_HELLO, hello);
        } catch (MessageDeliveryException e) {

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
            System.out.println("Attempting to reconnect...");
        }
    }

    private void safeConnect() {
        try {
            connect();
        } catch (Exception e) {
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying to reconnect
            } catch (InterruptedException ie) {
                //Nothing
            }
        }
    }
}
