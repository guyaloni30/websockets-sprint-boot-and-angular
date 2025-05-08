package com.example.websockets.mains;

public class Main1 {
    public static void main(String[] args) {
        try (MainClient c = new MainClient(1)) {
            c.run();
        }
    }
}
