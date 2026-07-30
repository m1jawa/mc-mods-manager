package io.github.m1jawa.mcmodsmanager.net;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;


public class HttpManager {

    private static final String USER_AGENT = "m1jawa/mc-mods-updater (https://github.com/m1jawa/mc-mods-updater)";
    private static final HttpClient DEFAULT_CLIENT = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

    private HttpManager() {}

    public static HttpResponse<String> sendRequest(String url) throws IOException, InterruptedException{
        return sendRequest(DEFAULT_CLIENT, url);
    }

    public static HttpResponse<String> sendRequest(HttpClient client, String url) throws IOException, InterruptedException{
        // requesting a mod
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();


        // looking for a response
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public static String download(String url, Path targetDir) throws IOException, InterruptedException{
        return download(DEFAULT_CLIENT, url, targetDir);
    }

    public static String download(HttpClient client, String url, Path targetDir) throws IOException, InterruptedException{
        // fetching filename and creating new path
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        Path targetFilePath = targetDir.resolve(fileName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        
        client.send(request, HttpResponse.BodyHandlers.ofFile(targetFilePath));

        return fileName;
    }
}
