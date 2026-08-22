package io.github.m1jawa.mcmodsmanager.model;

import io.github.m1jawa.mcmodsmanager.exceptions.UnknownLoaderException;

public enum ModLoader {
    FABRIC("fabric.mod.json"),
    FORGE("META-INF/mods.toml"),
    NEOFORGE("META-INF/neoforge.mods.toml"),
    QUILT("quilt.mod.json");

    private final String manifestPath;

    ModLoader(String manifestPath) {
        this.manifestPath = manifestPath;
    }

    public String getManifestPath() {
        return manifestPath;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    public static ModLoader fromString(String value) throws UnknownLoaderException {
        if (value == null) return null;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UnknownLoaderException("Unknown loader: " + value);
        }
    }

    public static String getValues() {

        StringBuilder sb = new StringBuilder();

        for (ModLoader modLoader : ModLoader.values()) {
            sb.append(modLoader.toString()).append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        return sb.toString();
    }
}