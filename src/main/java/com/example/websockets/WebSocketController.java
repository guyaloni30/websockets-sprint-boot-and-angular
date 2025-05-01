package com.example.websockets;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    @MessageMapping(Consts.HELLO_URI)
    @SendTo(Consts.TOPIC_GREETINGS)
    public MyWebsocketMessage greeting(MyWebsocketMessage msg) {
        return new MyWebsocketMessage(msg.id(), "Hello " + msg.text());
    }
}
