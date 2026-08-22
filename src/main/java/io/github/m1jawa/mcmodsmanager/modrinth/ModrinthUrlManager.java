package io.github.m1jawa.mcmodsmanager.modrinth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;

public class ModrinthUrlManager {

    private static final String BASE_URL = "https://api.modrinth.com/v2";

    private ModrinthUrlManager() {}

    public static String getModSearchUrl(String modName, String version, ModLoader modLoader) {

        String encodedModName = encode(modName);

        String facets = encode(
            String.format("[[\"versions:%s\"], [\"categories:%s\"], [\"project_type:mod\"]]", version, modLoader.toString()) // format modrinth requires
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

        // a bit of strange encoding; can't encode "=" and "&" symbols
        String gameVersionParameter = encodeParameter(version);
        String modLoaderParameter = encodeParameter(loader.toString());

        String facets = "game_versions=%s&loaders=%s".formatted(gameVersionParameter, modLoaderParameter);

        return "%s/project/%s/version?%s".formatted(BASE_URL, slug, facets);
    }

    // example = https://api.modrinth.com/v2/search?query=sodium%20extra&facets=[[%22categories:fabric%22],[%22versions:1.18.2%22],[%22project_type:mod%22]]&limit=3&offset=3

    public static String getModNameSearchUrl(String query, String version, ModLoader loader, int searchLimit, int page) {

        if (searchLimit <= 0) searchLimit = 100;
        if (page < 0) page = 0;

        String encodedModName = encode(query);

        String facets = encode(String.format("[[\"versions:%s\"], [\"categories:%s\"], [\"project_type:mod\"]]", version, loader));

        int offsetParameter = page * searchLimit;

        return "%s/search?query=%s&facets=%s&limit=%d&offset=%d".formatted(BASE_URL, encodedModName, facets, searchLimit, offsetParameter);
    }

    private static String encode(String s){
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String encodeParameter(String p) {
        return encode("[\"%s\"]".formatted(p));
    }
}