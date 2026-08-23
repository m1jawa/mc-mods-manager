package io.github.m1jawa.mcmodsmanager.minecraft;

import com.google.gson.*;

import java.io.FileReader;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

public class VersionParser {

    public static HashSet<String> parseVersionsToCache(JsonObject versions) {
        JsonArray versionsArray = versions.getAsJsonArray("versions");

        HashSet<String> versionSet = new HashSet<>((int) (versionsArray.size() / 0.75f) + 1);

        for (JsonElement element : versionsArray) {
            String id = element.getAsJsonObject().get("id").getAsString();
            versionSet.add(id);
        }

        return versionSet;
    }
}
