package io.github.m1jawa.mcmodsmanager.net;

import io.github.m1jawa.mcmodsmanager.model.IModData;

import java.nio.file.Path;

public interface ModDownloaderProvider {
    void downloadMod(IModData mod, String gameVersion, Path targetDir, boolean requiresSimilarityConfirmation) throws Exception;
}