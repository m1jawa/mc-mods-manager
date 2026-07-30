package io.github.m1jawa.mcmodsupdater.modrinth;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import io.github.m1jawa.mcmodsupdater.ModDownloaderProvider;
import io.github.m1jawa.mcmodsupdater.cli.ErrorsManager;
import io.github.m1jawa.mcmodsupdater.exceptions.ManifestNotFoundException;
import io.github.m1jawa.mcmodsupdater.exceptions.ModNotFoundException;
import io.github.m1jawa.mcmodsupdater.exceptions.UnexpectedResponseStructureException;
import io.github.m1jawa.mcmodsupdater.file.IOManager;
import io.github.m1jawa.mcmodsupdater.file.ModDataFetcher;
import io.github.m1jawa.mcmodsupdater.model.ModData;
import io.github.m1jawa.mcmodsupdater.net.HttpManager;

public class ModrinthService implements ModDownloaderProvider{

    private static final ModrinthService INSTANCE = new ModrinthService();

    private ModrinthService() {}

    public static ModrinthService getInstance() {
        return INSTANCE;
    }

    @Override
    public void downloadMod(ModData mod, String gameVersion, Path targetDir) throws IOException, InterruptedException, UnexpectedResponseStructureException, ModNotFoundException, ManifestNotFoundException{
        // requesting a slug
        String slug = getModSlug(mod, gameVersion); // first api request
        

        // requesting a mod
        String slugSearchUrl = ModrinthUrlManager.getModSlugSearchUrl(slug, gameVersion, mod.modLoader());
        String jsonResponse = fetchVersionsJson(slugSearchUrl); // second api request
        
        String downloadUrl = ModrinthJsonParser.extractDownloadUrlFromSearch(jsonResponse);

        if (downloadUrl == null) {
            throw new ModNotFoundException("No download link found for mod '%s' (%s, %s)".formatted(mod.name(), gameVersion, mod.modLoader()));
        }

        // downloading
        String fileName = HttpManager.download(downloadUrl, targetDir); // request to CDN, no need to use executeApiRequest()

        // checking if downloaded wrong mod
        try {
            ModData newMod = ModDataFetcher.fetchFabricModData(targetDir.resolve(fileName));

            if (!mod.id().replace('_', '-').equalsIgnoreCase(newMod.id().replace('_', '-'))) {
                IOManager.removeFile(targetDir.resolve(fileName));
                throw new ModNotFoundException("Downloaded mod ID '%s' does not match target mod ID '%s'".formatted(newMod.id(), mod.id()));
            }
        } catch (ManifestNotFoundException e) {
            IOManager.removeFile(targetDir.resolve(fileName));
            throw e;
        }
    }

    private static HttpResponse<String> executeApiRequest(String url) throws IOException, InterruptedException{
        ModrinthRateLimiter.acquirePermission();

        HttpResponse<String> response = HttpManager.sendRequest(url);

        if (response.statusCode() == 429) {
            long retryAfterSeconds = response.headers()
                    .firstValueAsLong("Retry-After")
                    .orElse(5L);

            ErrorsManager.printCustomMessage("[WARN] Rate limit (HTTP 429) hit for Modrinth. Waiting for %d seconds".formatted(retryAfterSeconds));

            Thread.sleep(retryAfterSeconds * 1000);
            return executeApiRequest(url); // Recursive retry
        }


        if (response.statusCode() != 200) {
            throw new IOException("Modrinth API error [%d] for URL: %s".formatted(response.statusCode(), url));
        }

        return response;
    }

    private static String getModSlug(ModData mod, String gameVersion) throws IOException, InterruptedException, UnexpectedResponseStructureException, ModNotFoundException{

        HttpResponse<String> response = executeApiRequest(ModrinthUrlManager.getModSearchUrl(mod, gameVersion));

        if (response.statusCode() != 200) {
            throw new ModNotFoundException("Failed to search mod. HTTP status: " + response.statusCode());
        }

        String jsonString = response.body();
        
        String slug = ModrinthJsonParser.extractSlugFromSearch(jsonString);

        if (slug == null) throw new ModNotFoundException("Can't find %s: HTTP response has empty hits or missing slug".formatted(mod.name()));

        return slug;
    }

    private static String fetchVersionsJson(String url) throws IOException, InterruptedException{

        ModrinthRateLimiter.acquirePermission();

        HttpResponse<String> response = executeApiRequest(url);

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch versions in '%s'. HTTP status: %s".formatted(url, response.statusCode()));
        }

        return response.body();
    }
}