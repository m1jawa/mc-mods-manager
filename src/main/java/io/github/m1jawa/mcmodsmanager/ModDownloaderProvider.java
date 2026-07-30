package io.github.m1jawa.mcmodsmanager;

import java.nio.file.Path;

import io.github.m1jawa.mcmodsmanager.model.ModData;

public interface ModDownloaderProvider {
    void downloadMod(ModData mod, String gameVersion, Path targetDir) throws Exception;
}