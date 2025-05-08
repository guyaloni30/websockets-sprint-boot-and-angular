package com.example.websockets.mains;

import com.example.websockets.client.WebSocketClient;

import java.util.Scanner;

public class MainClient extends WebSocketClient {
    public MainClient(int id) {
        super(id, "ws://localhost:8080/ws", false);
    }

    public void run() {
        // Keep sending messages until user types 'exit'
        try (Scanner scanner = new Scanner(System.in)) {
            safeConnect();
            while (true) {
                try {
                    System.out.println("Enter command (or 'exit' to quit):");
                    String command = scanner.nextLine().trim();
                    if (!command.isEmpty()) {
                        handleCommand(command);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Attempting to reconnect...");
                    safeConnect();
                }
            }
        }
    }

    @Override
    public void handleCommand(String command) {
        if ("exit".equalsIgnoreCase(command)) {
            disconnect();
            System.exit(-1);
            return;
        }
        if ("disconnect".equalsIgnoreCase(command)) {
            disconnect();
            return;
        }
        super.handleCommand(command);
    }

    private void safeConnect() {
        try {
            connect();
        } catch (Exception e) {
            try {
                Thread.sleep(5000); // Wait 5 seconds before trying to reconnect
            } catch (InterruptedException ie) {
                //Nothing
            }
        }
    }
}
