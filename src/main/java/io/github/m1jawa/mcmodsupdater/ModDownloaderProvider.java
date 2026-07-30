package io.github.m1jawa.mcmodsupdater;

import java.nio.file.Path;

import io.github.m1jawa.mcmodsupdater.model.ModData;

public interface ModDownloaderProvider {
    void downloadMod(ModData mod, String gameVersion, Path targetDir) throws Exception;
}