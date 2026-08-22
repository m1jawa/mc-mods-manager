package io.github.m1jawa.mcmodsmanager.modrinth;

import com.google.gson.*;

import io.github.m1jawa.mcmodsmanager.exceptions.UnexpectedResponseStructureException;
import io.github.m1jawa.mcmodsmanager.model.ModData;
import io.github.m1jawa.mcmodsmanager.model.ModLoader;

import java.util.*;

public class ModrinthJsonParser {
    private ModrinthJsonParser() {}

    public static String extractSlugFromSearch(String jsonString) throws UnexpectedResponseStructureException{
        JsonArray hits = getHitsFromResponse(jsonString);

        // fetching first mod
        JsonObject firstItem = hits.get(0).getAsJsonObject();

        // fetching slug
        if (firstItem.isEmpty() || !firstItem.has("slug") || firstItem.get("slug").isJsonNull()) return null;

        return firstItem.get("slug").getAsString();
        
    }

    public static String extractDownloadUrlFromSearch(String jsonString) throws UnexpectedResponseStructureException{
        // parsing string to json
        JsonElement element = JsonParser.parseString(jsonString);

        // checking structure (must be [{...}, {...}, ...] )
        if (!element.isJsonArray()) throw new UnexpectedResponseStructureException("Json response is not jsonArray: " + jsonString);

        JsonArray jsonResponse = element.getAsJsonArray();

        if (jsonResponse.isEmpty()) return null;

        // fetching lastest version of the mod
        JsonObject firstItem = jsonResponse.get(0).getAsJsonObject();

        if (!firstItem.has("files") || !firstItem.get("files").isJsonArray()) return null;

        JsonArray files = firstItem.get("files").getAsJsonArray();

        if (files.isEmpty()) return null;

        // fetching url
        String downloadUrl = files
            .get(0).getAsJsonObject()
            .get("url").getAsString();
        
        return downloadUrl;
    }

    //maps are contain names as keys and slugs as values
    public static List<ModData> extractModsDataFromSearch(String jsonString, ModLoader loader) throws UnexpectedResponseStructureException{

        List<ModData> mods = new ArrayList<>(); //LinkedHashMap, since the modifications are sorted by the number of downloads
        JsonArray hits = getHitsFromResponse(jsonString);

        if (hits == null) return mods;

        for (JsonElement item : hits) {
            try {
                mods.add( new ModData(
                        item.getAsJsonObject().get("slug").getAsString(),
                        item.getAsJsonObject().get("title").getAsString(),
                        loader,
                        item.getAsJsonObject().get("downloads").getAsInt(),
                        item.getAsJsonObject().get("description").getAsString()
                ));

            } catch (JsonSyntaxException e) {
                throw new UnexpectedResponseStructureException("Can't extract titles or slug from response: " + jsonString);
            }
        }

        return mods;
    }

    private static JsonArray getHitsFromResponse(String jsonString) throws UnexpectedResponseStructureException {
        // parsing string to json
        JsonElement element = JsonParser.parseString(jsonString);

        // checking structure (must be {"hits": [{...}, ...]})
        if (!element.isJsonObject()) throw new UnexpectedResponseStructureException("Json response is not jsonObject: " + jsonString);

        JsonObject jsonResponse = element.getAsJsonObject();

        if (!jsonResponse.has("hits")) return null;

        JsonArray hits = jsonResponse.get("hits").getAsJsonArray();

        if (hits.isEmpty()) return null;

        return hits;
    }
}
