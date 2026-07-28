package io.github.m1jawa.mcmodsupdater.file;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.m1jawa.mcmodsupdater.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsupdater.model.ModData;
import io.github.m1jawa.mcmodsupdater.model.ModLoader;

public class ModDataFetcher {

    private ModDataFetcher() {}

    public static ModData fetchFabricModData(String path) throws IOException, ManifestNotFoundException, InvalidPathException{
        return fetchFabricModData(Path.of(path));
    }

    public static ModData fetchFabricModData(Path path) throws IOException, ManifestNotFoundException{
        try (JarFile mod = new JarFile(path.toFile())) {
            JarEntry entry = mod.getJarEntry(ModLoader.FABRIC.getManifestPath());
            if (entry == null) throw new ManifestNotFoundException("Can't find fabric manifest in " + path.getFileName());

            try (InputStream stream = mod.getInputStream(entry);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                
                JsonObject manifest = JsonParser.parseReader(reader).getAsJsonObject();
                
                String id = manifest.has("id") ? manifest.get("id").getAsString() : null;
                String name = manifest.has("name") ? manifest.get("name").getAsString() : id;
                String version = manifest.has("version") ? manifest.get("version").getAsString() : "unknown";

                return new ModData(id, name, ModLoader.FABRIC, version);
            }
        }
    }
}
