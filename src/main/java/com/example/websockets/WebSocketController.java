package com.example.websockets;

import lombok.AllArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/greeting")
    @SendTo("/topic/greeting")
    public MyWebsocketMessage greeting(MyWebsocketMessage msg, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("greeting " + msg);
        return new MyWebsocketMessage(headerAccessor.getSessionId(), msg.id(), "Greeting to " + msg.text() + " who joined the party");
    }

    @MessageMapping("/hello")
    public void hello(MyWebsocketMessage msg, SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("hello " + msg);
        String sessionId = headerAccessor.getSessionId();
        messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/hello",
                new MyWebsocketMessage(
                        sessionId,
                        msg.id(),
                        "Hello " + msg.text() + ", how can I help you?"),
                createHeaders(sessionId));
    }

    private static MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
