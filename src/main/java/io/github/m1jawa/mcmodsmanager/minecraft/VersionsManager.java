package io.github.m1jawa.mcmodsmanager.minecraft;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.github.m1jawa.mcmodsmanager.file.CacheManager;
import io.github.m1jawa.mcmodsmanager.net.HttpManager;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashSet;

public class VersionsManager {
    VersionsManager() {}

    private static final Gson GSON = new Gson();
    private static final String MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public static HashSet<String> updateCache() throws IOException, InterruptedException {
        JsonObject versions = fetchVersionManifest();
        HashSet<String> cache = VersionParser.parseVersionsToCache(versions);

        CacheManager.saveCache(cache);
        return cache;
    }

    public static boolean checkIfPresents(String version) {
        HashSet<String> cache = CacheManager.loadCache();
        if (cache == null) return false;
        return cache.contains(version);
    }

    private static JsonObject fetchVersionManifest() throws IOException, InterruptedException {
        HttpResponse<String> response = HttpManager.sendRequest(MANIFEST_URL);
        return GSON.fromJson(response.body(), JsonObject.class);
    }
}
