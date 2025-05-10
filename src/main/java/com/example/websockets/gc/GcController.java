package com.example.websockets.gc;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * To see memory consumption before and after GC, use the following command: http://localhost:8080/api/gc
 */
@RestController
@RequestMapping(path = "api/gc", produces = "application/json")
public class GcController {
    private static final Runtime runtime = Runtime.getRuntime();

    @GetMapping
    public GcStatus gc() {
        long before = (runtime.totalMemory() - runtime.freeMemory()) / 1_000_000;
        System.gc();
        long after = (runtime.totalMemory() - runtime.freeMemory()) / 1_000_000;
        System.out.println("GC!!! from " + before + " to " + after);
        return new GcStatus(before, after);
    }
}
