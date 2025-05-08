package com.example.websockets.mains;

public class Main3 {
    public static void main(String[] args) {
        try (MainClient c = new MainClient(3)) {
            c.run();
        }
    }
}
