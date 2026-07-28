package io.github.m1jawa.mcmodsupdater.model;

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
}