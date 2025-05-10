package com.example.websockets.websockets.load;

import com.example.websockets.websockets.Consts;
import com.example.websockets.websockets.service.WebSocketsService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class LoadController {
    private final WebSocketsService webSocketsService;

    @MessageMapping(Consts.REQUEST_LOAD)
    public void hello(LoadRequest msg, SimpMessageHeaderAccessor headerAccessor) throws InterruptedException {
        if (msg.delayMillis() > 0) {
            Thread.sleep(msg.delayMillis());
        }
        int a = msg.a();
        int b = msg.b();
        webSocketsService.sendTo(
                headerAccessor,
                Consts.QUEUE_PREFIX + Consts.RESPONSE_TO_LOAD,
                new LoadResponse(
                        a + b,
                        a - b,
                        a * b,
                        (b != 0) ? a / b : 0));
    }
}
