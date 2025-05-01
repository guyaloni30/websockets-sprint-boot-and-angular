package com.example.websockets;

import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableScheduling
public class WebSocketEventConfig {
//    private final Set<String> sessions = Collections.synchronizedSet(new TreeSet<>());
//
//    @Scheduled(fixedRate = 5000)
//    public void print() {
//        System.out.println(sessions.size() + " concurrent sessions: " + sessions);
//    }
//
//    @Bean
//    public ApplicationListener<SessionConnectEvent> connectEventListener(/*SimpMessageSendingOperations messagingTemplate*/) {
//        return event -> {
//            String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
//            System.out.println("Client connected: " + sessionId);
//            sessions.add(sessionId);
////            // Optional: Broadcast connection event to admin topic
////            messagingTemplate.convertAndSend("/topic/admin/connect",
////                    Map.of("sessionId", sessionId, "event", "connected", "timestamp", System.currentTimeMillis()));
//        };
//    }
//
//    @Bean
//    public ApplicationListener<SessionDisconnectEvent> disconnectEventListener(/*SimpMessageSendingOperations messagingTemplate*/) {
//        return event -> {
//            String sessionId = event.getSessionId();
//            System.out.println("Client disconnected: " + sessionId);
//            sessions.remove(sessionId);
////            // Optional: Broadcast disconnection event to admin topic
////            messagingTemplate.convertAndSend("/topic/admin/disconnect",
////                    Map.of("sessionId", sessionId, "event", "disconnected", "timestamp", System.currentTimeMillis()));
//        };
//    }
}
