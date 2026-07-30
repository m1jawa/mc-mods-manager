package io.github.m1jawa.mcmodsmanager.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.github.m1jawa.mcmodsmanager.cli.ErrorsManager;
import io.github.m1jawa.mcmodsmanager.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;

public class ModsScanner {
    private ModsScanner() {}

    public static List<Path> scanDirectory(Path path) throws IOException{

        try (Stream<Path> stream = Files.list(path)) {
            return stream.filter(Files::isRegularFile)
                .filter(file -> file.toString().endsWith(".jar"))
                .toList();
        }
    }

    public static List<ModData> fetchAllFromDirectory(Path dir, ModLoader loader) throws IOException{
        switch (loader) {
            case ModLoader.FABRIC -> {
                return fetchFabricModsDataFromDirectory(dir);
            }
            case ModLoader.QUILT -> {
            }
            case ModLoader.FORGE -> {
            }
            case ModLoader.NEOFORGE -> {
            }
        }
        return null;
    }

    private static List<ModData> fetchFabricModsDataFromDirectory(Path dir) throws IOException{
        // getting list of .jar files
        List<Path> mods = scanDirectory(dir);

        // getting the manifest
        List<ModData> modsData = mods.stream()
                .map(path -> {
                    try {
                        return ModDataFetcher.fetchFabricModData(path);
                    } catch (IOException e) {
                        ErrorsManager.printCustomMessage("Got an IO Exception while fetching %s data: %s".formatted(path.getFileName().toString(), e.getMessage()));
                        return null;
                    } catch (ManifestNotFoundException e) {
                        ErrorsManager.printExceptionMessage(e);
                        return null;
                    }
                }).filter(Objects::nonNull).toList();

        return modsData;
    }
}
