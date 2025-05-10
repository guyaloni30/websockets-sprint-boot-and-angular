package com.example.websockets.mains;

import com.example.websockets.client.WebSocketClient;

public class Main1 {
    public static void main(String[] args) {
        try (MainWebSocketClient c = new MainWebSocketClient(1)) {
            c.run();
        }
        WebSocketClient.shutdown();
    }
}
