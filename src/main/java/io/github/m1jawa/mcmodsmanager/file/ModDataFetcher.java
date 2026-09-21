package io.github.m1jawa.mcmodsmanager.file;

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

import io.github.m1jawa.mcmodsmanager.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;

public class ModDataFetcher {

    private ModDataFetcher() {}

    public static ModData fetchFabricModData(Path path) throws IOException, ManifestNotFoundException { //entry point
        try (JarFile mod = new JarFile(path.toFile())) {
            JarEntry entry = mod.getJarEntry(ModLoader.FABRIC.getManifestPath());
            return fetchFabricModData(path, entry, mod.getInputStream(entry));
        }
    }

    public static ModData fetchFabricModData(Path path, JarEntry entry, InputStream is) throws IOException, ManifestNotFoundException{ //only data extraction
        if (entry == null) throw new ManifestNotFoundException("Can't find fabric manifest in " + path.getFileName());

        try (InputStream stream = is;
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

            JsonObject manifest = JsonParser.parseReader(reader).getAsJsonObject();

            String id = manifest.has("id") ? manifest.get("id").getAsString() : null;
            String name = manifest.has("name") ? manifest.get("name").getAsString() : id;

            return new ModData(id, name, ModLoader.FABRIC);
        }
    }

    public static ModData fetchQuiltModData(Path path) throws IOException, ManifestNotFoundException{
        try (JarFile mod = new JarFile(path.toFile())) {
            JarEntry entry = mod.getJarEntry(ModLoader.QUILT.getManifestPath());
            if (entry == null) { // no quilt manifest, may be fabric

                entry = mod.getJarEntry(ModLoader.FABRIC.getManifestPath());
                return fetchFabricModData(path, entry, mod.getInputStream(entry)); // method will return ManifestNotFoundException if there's no fabric manifest

            }

            //extracting data from quilt mf
            try (InputStream stream = mod.getInputStream(entry);
                 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {

                JsonObject manifest = JsonParser.parseReader(reader).getAsJsonObject().get("quilt_loader").getAsJsonObject();

                String id = manifest.has("id") ? manifest.get("id").getAsString() : null;

                if (!manifest.has("metadata")) return new ModData(id, id, ModLoader.QUILT);
                JsonObject metadata = manifest.get("metadata").getAsJsonObject();

                String name = metadata.has("name") ? metadata.get("name").getAsString() : id;

                return new ModData(id, name, ModLoader.QUILT);
            }
        }
    }

    //todo fix: stops if cant find a manifest
    public static ModData fetchModData(Path path, ModLoader loader) throws IOException, ManifestNotFoundException{
        switch (loader) {
            case FABRIC:
                return fetchFabricModData(path);
            case QUILT:
                return fetchQuiltModData(path);
            default:
                return fetchQuiltModData(path);
        }
    }

    //debug
    public static void main(String[] args){
        try {
            System.out.println (
                    fetchFabricModData(Path.of("/home/m1jawa/Desktop/mc-mods-manager/dev/quilt/sodium-fabric-0.5.13-mc1.20.1.jar"))
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
