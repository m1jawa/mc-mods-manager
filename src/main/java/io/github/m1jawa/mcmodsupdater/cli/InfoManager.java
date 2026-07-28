package io.github.m1jawa.mcmodsupdater.cli;


import java.net.http.HttpResponse;


public class InfoManager {
    
    private InfoManager() {}

    public static void printHttpResponse(HttpResponse<String> response) {
        
        if (response == null) {
            ErrorsManager.printCustomMessage("Can't find mods: response is null");
            return;
        }

        if (response.statusCode() >= 400) {
            ErrorsManager.printCustomMessage("API returned error code: " + response.statusCode());
            return;
        }

        System.out.println("Response: " + response.body());
    }
}
