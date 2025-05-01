package com.example.websockets;

import com.example.websockets.messages.MyWebsocketMessage;
import lombok.AllArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;

@Controller
@AllArgsConstructor
public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping(Consts.REQUEST_GREETING)
    @SendTo(Consts.TOPIC_PREFIX + Consts.TOPIC_JOIN)
    public MyWebsocketMessage greeting(MyWebsocketMessage msg, SimpMessageHeaderAccessor headerAccessor) {
        if (ObjectUtils.isEmpty(msg.sessionId())) {
            msg = new MyWebsocketMessage(headerAccessor.getSessionId(), msg.id(), msg.text());
        }
        System.out.println("greeting " + msg);
        return new MyWebsocketMessage(headerAccessor.getSessionId(), msg.id(), "Greeting to " + msg.text() + " who joined the party");
    }

    @MessageMapping(Consts.REQUEST_HELLO)
    public void hello(MyWebsocketMessage msg, SimpMessageHeaderAccessor headerAccessor) {
        if (ObjectUtils.isEmpty(msg.sessionId())) {
            msg = new MyWebsocketMessage(headerAccessor.getSessionId(), msg.id(), msg.text());
        }
        System.out.println("hello " + msg);
        String sessionId = headerAccessor.getSessionId();
        messagingTemplate.convertAndSendToUser(
                sessionId,
                Consts.QUEUE_PREFIX + Consts.RESPONSE_TO_HELLO,
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
