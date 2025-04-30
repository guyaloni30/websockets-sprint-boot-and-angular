package com.example.websockets;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {
    //    private static final JsonMapper jsonMapper = new JsonMapper();
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public WebsocketResponse greeting(String name) {
        return new WebsocketResponse(1, "Hello " + name);
//        System.out.println(response);
//        try {
//            return jsonMapper.writeValueAsString(response);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Can't marshal " + response, e);
//        }
    }
}
