package com.example.websockets.mains;

public class Main2 {
    public static void main(String[] args) {
        try (MainClient c = new MainClient(2)) {
            c.run();
        }
    }
}
