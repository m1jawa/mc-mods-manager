package io.github.m1jawa.mcmodsupdater.net;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class HttpManager {

    private static final String USER_AGENT = "m1jawa/mc-mods-updater";
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
        // requesting mods
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();


        // looking for a response
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
