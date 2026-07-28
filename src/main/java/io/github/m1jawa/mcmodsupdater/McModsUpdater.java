package io.github.m1jawa.mcmodsupdater;


import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import io.github.m1jawa.mcmodsupdater.cli.ErrorsManager;
import io.github.m1jawa.mcmodsupdater.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsupdater.file.ModDataFetcher;
import io.github.m1jawa.mcmodsupdater.file.ModsScanner;
import io.github.m1jawa.mcmodsupdater.model.ModData;


public class McModsUpdater {

    public static void main(String[] args) {

        Path modDir = Path.of("testmods");

        try {
            List<Path> mods = ModsScanner.scanDirectory(modDir);

            mods.forEach(path -> {
                    try {
                        ModData mod = ModDataFetcher.fetchFabricModData(path);
                        System.out.println("file: %s; id: %s; name: %s".formatted(path.getFileName(), mod.id(), mod.name()));
                    } catch (IOException e) {
                        ErrorsManager.printCustomMessage("Got an IO Exception while fetching %s data: %s".formatted(path.getFileName(), e.getMessage()));
                    } catch (ManifestNotFoundException e) {
                        ErrorsManager.printExceptionMessage(e);
                    }
                });

        } catch (IOException e) {
            ErrorsManager.printCustomMessage("Failed to scan mods directory: " + e.getMessage());
        }
    }
}