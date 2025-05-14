package com.example.websockets.hello;

import com.example.websockets.client.WebSocketClient;

public class Hello1 {
    public static void main(String[] args) {
        try (MainWebSocketClient c = new MainWebSocketClient(1)) {
            c.run();
        }
        WebSocketClient.shutdown();
    }
}
