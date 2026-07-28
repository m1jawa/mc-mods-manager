package io.github.m1jawa.mcmodsupdater;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.github.m1jawa.mcmodsupdater.model.ModData;
import io.github.m1jawa.mcmodsupdater.model.ModLoader;

public class ModrinthApiManager {

    // url to search mods via api
    private static final String SEARCH_URL = "https://api.modrinth.com/v2/search?query=";

    private ModrinthApiManager() {}

    public static String getModSearchUrl(String modName, String version, ModLoader modLoader) {

        String encodedModName = URLEncoder.encode(modName, StandardCharsets.UTF_8);

        String facets = URLEncoder.encode(
            String.format("[[\"versions:%s\"], [\"categories:%s\"]]", version, modLoader.toString()), // format modrinth requiers
            StandardCharsets.UTF_8 // encoding
        );

        return SEARCH_URL + encodedModName + "&facets=" + facets;
    }

    public static String getModSearchUrl(ModData modData, String gameVersion) {
        return getModSearchUrl(
            modData.name(), 
            gameVersion, 
            modData.modLoader()
        );
    }
}