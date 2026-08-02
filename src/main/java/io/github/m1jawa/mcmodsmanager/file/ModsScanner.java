package io.github.m1jawa.mcmodsmanager.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

    public static List<ModData> fetchAllFromDirectory(Path dir, ModLoader loader) throws IOException, ManifestNotFoundException{
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

    private static List<ModData> fetchFabricModsDataFromDirectory(Path dir) throws IOException, ManifestNotFoundException{
        // getting list of .jar files
        List<Path> mods = scanDirectory(dir);

        // getting the manifest
        List<ModData> modsData = new ArrayList<>();

        for (Path path : mods) {
                try {
                    modsData.add( ModDataFetcher.fetchFabricModData(path) );
                } catch (IOException e) {
                    throw new IOException("Got an IO Exception while fetching %s data: %s".formatted(path.getFileName().toString(), e.getMessage()));
                } catch (ManifestNotFoundException e) {
                    throw new ManifestNotFoundException("Can't find %s manifest".formatted(path.getFileName()));
                }
                
        }
        return modsData;
    }
}
