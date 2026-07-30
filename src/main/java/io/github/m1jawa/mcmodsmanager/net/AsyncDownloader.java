package io.github.m1jawa.mcmodsmanager.net;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.m1jawa.mcmodsmanager.ModDownloaderProvider;
import io.github.m1jawa.mcmodsmanager.cli.ErrorsManager;
import io.github.m1jawa.mcmodsmanager.model.ModData;

public class AsyncDownloader {

    private static final int MAX_PARALLEL_DOWNLOADS = 3;

    private AsyncDownloader() {}

    public static void downloadAllViaModrinth(List<ModData> mods, String gameVersion, Path targetDir, ModDownloaderProvider provider) {
        Semaphore downloadSemaphore = new Semaphore(MAX_PARALLEL_DOWNLOADS);
        AtomicInteger completedCount = new AtomicInteger(0);
        int totalMods = mods.size();

        try (ExecutorService executor = Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS)) {

            List<CompletableFuture<Void>> futures = mods.stream()
                .map(mod -> CompletableFuture.runAsync(() -> {
                    try {
                        downloadSemaphore.acquire();

                        provider.downloadMod(mod, gameVersion, targetDir);

                        int current = completedCount.incrementAndGet();
                        System.out.printf("[%d/%d] Successfully updated: %s%n", current, totalMods, mod.name());

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        ErrorsManager.printCustomMessage("Download interrupted for: " + mod.name());

                    } catch (Exception e) {
                        int current = completedCount.incrementAndGet();
                        ErrorsManager.printCustomMessage("[%d/%d] Failed to update %s: %s"
                                .formatted(current, totalMods, mod.name(), e.getMessage()));

                    } finally {
                        downloadSemaphore.release();
                    }
                }, executor))
                .toList();

            // blocking main thread until download complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }
}