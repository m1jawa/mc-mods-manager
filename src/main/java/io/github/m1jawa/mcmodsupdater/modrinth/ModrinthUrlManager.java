package io.github.m1jawa.mcmodsupdater.modrinth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.github.m1jawa.mcmodsupdater.model.ModData;
import io.github.m1jawa.mcmodsupdater.model.ModLoader;

public class ModrinthUrlManager {

    private static final String BASE_URL = "https://api.modrinth.com/v2";

    private ModrinthUrlManager() {}

    public static String getModSearchUrl(String modName, String version, ModLoader modLoader) {

        String encodedModName = encode(modName);

        String facets = URLEncoder.encode(
            String.format("[[\"versions:%s\"], [\"categories:%s\"]]", version, modLoader.toString()), // format modrinth requiers
            StandardCharsets.UTF_8 // encoding
        );

        return "%s/search?query=%s&facets=%s".formatted(BASE_URL, encodedModName, facets);
    }

    public static String getModSearchUrl(ModData modData, String gameVersion) {
        return getModSearchUrl(
            modData.name(), 
            gameVersion, 
            modData.modLoader()
        );
    }

    public static String getModSlugSearchUrl(String slug, String version, ModLoader loader) {

        // a bit of strande encoding; can't encode "=" and "&" symbols
        String gameVersionParameter = encode("[\"%s\"]".formatted(version));
        String modLoaderParameter = encode("[\"%s\"]".formatted(loader));

        String facets = "game_versions=%s&loaders=%s".formatted(gameVersionParameter, modLoaderParameter);

        return "%s/project/%s/version?%s".formatted(BASE_URL, slug, facets);
    }

    private static String encode(String s){
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}