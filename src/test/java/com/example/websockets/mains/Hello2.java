package com.example.websockets.mains;

import com.example.websockets.client.WebSocketClient;

public class Hello2 {
    public static void main(String[] args) {
        try (MainWebSocketClient c = new MainWebSocketClient(2)) {
            c.run();
        }
        WebSocketClient.shutdown();
    }
}
