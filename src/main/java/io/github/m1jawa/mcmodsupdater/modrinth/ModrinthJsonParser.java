package io.github.m1jawa.mcmodsupdater.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.m1jawa.mcmodsupdater.exceptions.UnexpectedResponseStructureException;

public class ModrinthJsonParser {
    private ModrinthJsonParser() {}

    public static String extractSlugFromSearch(String jsonString) throws UnexpectedResponseStructureException{
        // parsing string to json
        JsonElement element = JsonParser.parseString(jsonString);
        
        // checking structure (must be {"hits": [{...}, ...]})
        if (!element.isJsonObject()) throw new UnexpectedResponseStructureException("Json response is not jsonObject: " + jsonString);

        JsonObject jsonResponse = element.getAsJsonObject();

        if (!jsonResponse.has("hits")) return null;

        JsonArray hits = jsonResponse.get("hits").getAsJsonArray();

        if (hits.isEmpty()) return null;

        // fetching first mod
        JsonObject firstItem = hits.get(0).getAsJsonObject();

        // fetching slug
        if (firstItem.isEmpty() || !firstItem.has("slug") || firstItem.get("slug").isJsonNull()) return null;

        String slug = firstItem.get("slug").getAsString();
        return slug;
        
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
}
