package com.example.websockets;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@Component
@EnableScheduling
@AllArgsConstructor
public class WebSocketEventListener {
    private final Set<String> sessions = Collections.synchronizedSet(new TreeSet<>());

    private final SimpMessageSendingOperations messagingTemplate;

    @Scheduled(fixedRate = 5000)
    public void print() {
        System.out.println(sessions.size() + " concurrent sessions: " + sessions);
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        System.out.println("Client connected: " + sessionId);
        sessions.add(sessionId);

        // You can store connected clients in a map/database if needed
        // connectedClients.put(sessionId, new ClientInfo(...));
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        System.out.println("Client disconnected: " + sessionId);
        sessions.remove(sessionId);

        // Clean up any resources associated with this session
        // connectedClients.remove(sessionId);

        // You can also notify other users if needed
//        messagingTemplate.convertAndSend("/topic/users/status",
//                Map.of("sessionId", sessionId, "status", "offline"));
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        String destination = event.getMessage().getHeaders().get("simpDestination", String.class);
        System.out.println("Client " + sessionId + " subscribed to " + destination);
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        String destination = event.getMessage().getHeaders().get("simpSubscriptionId", String.class);
        System.out.println("Client " + sessionId + " unsubscribed from " + destination);
    }
}
