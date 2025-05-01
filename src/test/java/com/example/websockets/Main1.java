package com.example.websockets;

import com.example.websockets.client.WebSocketClient;

public class Main1 {
    public static void main(String[] args) {
        new WebSocketClient(111).run();
    }
}
