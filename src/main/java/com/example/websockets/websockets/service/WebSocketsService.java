package com.example.websockets.websockets.service;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class WebSocketsService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendAllJson(String destination, Object message) {
        sendAllJson(destination, message, null);
    }

    public void sendAllJson(String destination, Object message, Map<String, Object> headers) {
        headers = new HashMap<>((headers != null) ? headers : Map.of());
        headers.put("content-type", MediaType.APPLICATION_JSON_VALUE);
        sendAll(destination, message, headers);
    }

    public void sendAll(String destination, Object message, Map<String, Object> headers) {
        messagingTemplate.convertAndSend(destination, message, headers);
    }

    public void sendTo(SimpMessageHeaderAccessor headerAccessor, String destination, Object message) {
        String sessionId = headerAccessor.getSessionId();
        messagingTemplate.convertAndSendToUser(
                sessionId,
                destination,
                message,
                createHeaders(sessionId));
    }

    private static MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
