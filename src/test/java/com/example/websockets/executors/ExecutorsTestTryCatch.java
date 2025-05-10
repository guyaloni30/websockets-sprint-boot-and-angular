package com.example.websockets.executors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsTestTryCatch {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        try (ExecutorService es = Executors.newFixedThreadPool(20)) {
            for (int i = 0; i < 100; i++) {
                int index = i;
                es.submit(() -> {
                    try {
                        Thread.sleep(1000);
                        System.out.println(index);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            System.out.println("done");
        }
        System.out.println("Time: " + (System.currentTimeMillis() - start));
    }
}
