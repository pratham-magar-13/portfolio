package com.jackdaw.portfolio.web;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory sliding-window rate limiter for the public contact form.
 * Suitable for a low-traffic portfolio site; for multi-instance deployments
 * replace with a shared store (e.g. Redis).
 */
@Component
public class ContactRateLimiter {

    private final int maxAttempts;
    private final Duration window;
    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public ContactRateLimiter() {
        this(5, Duration.ofHours(1));
    }

    ContactRateLimiter(int maxAttempts, Duration window) {
        this.maxAttempts = maxAttempts;
        this.window = window;
    }

    public synchronized boolean allow(String key) {
        long now = System.currentTimeMillis();
        Deque<Long> times = hits.computeIfAbsent(key, k -> new ArrayDeque<>());
        while (!times.isEmpty() && now - times.peekFirst() > window.toMillis()) {
            times.pollFirst();
        }
        if (times.size() >= maxAttempts) {
            return false;
        }
        times.addLast(now);
        return true;
    }
}
