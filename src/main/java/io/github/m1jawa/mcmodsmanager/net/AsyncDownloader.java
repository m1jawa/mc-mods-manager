package io.github.m1jawa.mcmodsmanager.net;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.m1jawa.mcmodsmanager.ModDownloaderProvider;
import io.github.m1jawa.mcmodsmanager.cli.InfoManager;
import io.github.m1jawa.mcmodsmanager.exceptions.ModNotFoundException;
import io.github.m1jawa.mcmodsmanager.model.InfoType;
import io.github.m1jawa.mcmodsmanager.model.LoadedModsData;
import io.github.m1jawa.mcmodsmanager.model.ModData;

public class AsyncDownloader {

    private static final int MAX_PARALLEL_DOWNLOADS = 3;

    private AsyncDownloader() {}

    public static LoadedModsData downloadAllViaModrinth(List<ModData> mods, String gameVersion, Path targetDir, ModDownloaderProvider provider) {
        Semaphore downloadSemaphore = new Semaphore(MAX_PARALLEL_DOWNLOADS);
        AtomicInteger completedCount = new AtomicInteger(0);
        int totalMods = mods.size();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<ModData> failedMods = new ConcurrentLinkedQueue<ModData>();

        try (ExecutorService executor = Executors.newFixedThreadPool(MAX_PARALLEL_DOWNLOADS)) {

            List<CompletableFuture<Void>> futures = mods.stream()
                .map(mod -> CompletableFuture.runAsync(() -> {
                    try {
                        downloadSemaphore.acquire();

                        provider.downloadMod(mod, gameVersion, targetDir);

                        successCount.incrementAndGet();
                        int current = completedCount.incrementAndGet();
                        
                        InfoManager.log("[%d/%d] Successfully updated: %s".formatted(current, totalMods, mod.name()), InfoType.INFO);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();

                        int current = completedCount.incrementAndGet();
                        InfoManager.log(
                            "[%d/%d] Download interrupted for ".formatted(current, totalMods, mod.name(), e.getMessage()) + mod.name(),
                            InfoType.ERROR
                        );

                        failedCount.incrementAndGet();
                        failedMods.add(mod);

                    } catch (ModNotFoundException e) {
                        int current = completedCount.incrementAndGet();

                        InfoManager.log(
                            "[%d/%d] Failed to update %s: %s".formatted(current, totalMods, mod.name(), e.getMessage()),
                            InfoType.ERROR
                        );

                        InfoManager.log(
                            "[%d/%d] Deleted %s because of unexpected mod ID".formatted(current, totalMods, mod.name()),
                            InfoType.INFO
                        );

                        failedCount.incrementAndGet();
                        failedMods.add(mod);
                        
                    } catch (Exception e) {
                        int current = completedCount.incrementAndGet();
                        InfoManager.log(
                            "[%d/%d] Failed to update %s: %s".formatted(current, totalMods, mod.name(), e.getMessage()),
                            InfoType.ERROR
                        );

                        failedCount.incrementAndGet();
                        failedMods.add(mod);

                    } finally {
                        downloadSemaphore.release();
                    }
                }, executor))
                .toList();

            // blocking main thread until download complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            
            List<ModData> failedModsList = new ArrayList<>(failedMods);

            return new LoadedModsData(totalMods, successCount.get(), failedCount.get(), failedModsList);
        }
    }
}