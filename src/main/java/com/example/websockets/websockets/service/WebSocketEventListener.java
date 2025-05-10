package com.example.websockets.websockets.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebSocketEventListener {
    private static final Runtime runtime = Runtime.getRuntime();
    private final Set<String> sessions = Collections.synchronizedSet(new TreeSet<>());

    private final SimpMessageSendingOperations messagingTemplate;

    private int lastSessions;

    @Scheduled(fixedRate = 1000)
    public void print() {
        int current = sessions.size();
        int diff = current - lastSessions;
        long mb = (runtime.totalMemory() - runtime.freeMemory()) / 1_000_000;
        String status = current + " concurrent sessions";
        if (diff != 0) {
            status += ", " + Math.abs(diff) + " " + ((diff > 0) ? "added" : "removed");
        }
        System.out.printf("Using %5d MB, %s%n", mb, status);
        lastSessions = sessions.size();
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
//        System.out.println("Client connected: " + sessionId);
        sessions.add(sessionId);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
//        System.out.println("Client disconnected: " + sessionId);
        sessions.remove(sessionId);
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        String destination = event.getMessage().getHeaders().get("simpDestination", String.class);
//        System.out.println("Client " + sessionId + " subscribed to " + destination);
    }

    @EventListener
    public void handleUnsubscribeEvent(SessionUnsubscribeEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        String destination = event.getMessage().getHeaders().get("simpSubscriptionId", String.class);
//        System.out.println("Client " + sessionId + " unsubscribed from " + destination);
    }
}
