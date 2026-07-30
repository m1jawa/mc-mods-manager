package io.github.m1jawa.mcmodsupdater.modrinth;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ModrinthRateLimiter {

    // 1 mod = 2 requests; 2 req/mod * 2 mod/sec = 4 req/sec = 240 req/min; 240 < rate limit (300)
    private static final int MAX_REQUEST_PER_SECOND = 4;
    private static final Semaphore SEMAPHORE = new Semaphore(MAX_REQUEST_PER_SECOND);
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    static {
        // Reset MAX_REQUEST_PER_SECOND back to 4 very second
        SCHEDULER.scheduleAtFixedRate(() -> {
            int avalible = SEMAPHORE.availablePermits();
            if (avalible < MAX_REQUEST_PER_SECOND) SEMAPHORE.release(MAX_REQUEST_PER_SECOND - avalible);
        }, 1, 1, TimeUnit.SECONDS);
    }

    private ModrinthRateLimiter() {}

    public static void acquirePermission() throws InterruptedException{
        SEMAPHORE.acquire();
    }
}
